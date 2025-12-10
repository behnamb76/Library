package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.*;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.PayPenaltyRequest;
import ir.bahman.library.dto.PaymentDTO;
import ir.bahman.library.model.*;
import ir.bahman.library.model.enums.*;
import ir.bahman.library.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String librarianToken;
    private String memberToken;
    private Long penaltyId;
    private Long memberId;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role librarianRole = roleRepository.findByName("LIBRARIAN").orElseThrow();
        Role memberRole = roleRepository.findByName("MEMBER").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        Person admin = createPerson("Admin", "User", "1111111111", "09111111111", List.of(adminRole, userRole));
        Account adminAcc = createAccount("admin1", "admin1", admin, adminRole);
        Map<String, String> adminTokens = authService.login(new LoginRequest("admin1", "admin1"));
        adminToken = adminTokens.get("accessToken");

        Person librarian = createPerson("Lib", "Rarian", "2222222222", "09222222222", List.of(librarianRole, userRole));
        Account libAcc = createAccount("librarian", "Pass1234", librarian, librarianRole);
        Map<String, String> libTokens = authService.login(new LoginRequest("librarian", "Pass1234"));
        librarianToken = libTokens.get("accessToken");

        Person member = createPerson("Member", "User", "3333333333", "09333333333", List.of(memberRole, userRole));
        Account memberAcc = createAccount("member", "Pass1234", member, memberRole);
        Map<String, String> memberTokens = authService.login(new LoginRequest("member", "Pass1234"));
        memberToken = memberTokens.get("accessToken");
        memberId = member.getId();

        Category category = categoryRepository.save(Category.builder().name("Fiction").deleted(false).build());
        Book book = Book.builder()
                .title("Overdue Book")
                .author("Author")
                .isbn("978-3-16-148410-0")
                .publisher("Pub")
                .publicationYear(Year.of(2020))
                .replacementCost(BigDecimal.valueOf(100_000))
                .category(category)
                .deleted(false)
                .build();
        book = bookRepository.save(book);
        BookCopy copy = BookCopy.builder()
                .book(book)
                .status(BookCopyStatus.LOANED)
                .deleted(false)
                .build();
        copy = bookCopyRepository.save(copy);

        Loan loan = Loan.builder()
                .member(member)
                .bookCopy(copy)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6))
                .status(LoanStatus.OVERDUE)
                .deleted(false)
                .build();
        loan = loanRepository.save(loan);

        Penalty penalty = Penalty.builder()
                .loan(loan)
                .reason(PenaltyReason.OVERDUE)
                .status(PenaltyStatus.UNPAID)
                .amount(BigDecimal.valueOf(18000)) // 6 days × 3000
                .lastCalculatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
        penalty = penaltyRepository.save(penalty);
        penaltyId = penalty.getId();
    }

    @AfterEach
    void tearDown() {
        penaltyRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookCopyRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        personRepository.deleteAllInBatch();
    }

    private Person createPerson(String firstName, String lastName, String nationalCode, String phone, List<Role> roles) {
        Person p = Person.builder()
                .firstName(firstName)
                .lastName(lastName)
                .nationalCode(nationalCode)
                .phoneNumber(phone)
                .birthday(LocalDate.of(1990, 1, 1))
                .roles(roles)
                .deleted(false)
                .build();
        return personRepository.save(p);
    }

    private Account createAccount(String username, String rawPassword, Person person, Role activeRole) {
        Account acc = Account.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .status(AccountStatus.ACTIVE)
                .person(person)
                .activeRole(activeRole)
                .authId(UUID.randomUUID())
                .deleted(false)
                .build();
        return accountRepository.save(acc);
    }

    @Test
    void testPayPenalty() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("CASH");

        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(18000))
                .andExpect(jsonPath("$.method").value("CASH"))
                .andExpect(jsonPath("$.paymentFor").value("PENALTY"));

        Payment payment = paymentRepository.findByPenaltyId(penaltyId).orElseThrow();
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(18000));
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(payment.getPaymentFor()).isEqualTo(PaymentFor.PENALTY);
        assertThat(payment.getMember().getId()).isEqualTo(memberId);

        Penalty updatedPenalty = penaltyRepository.findById(penaltyId).orElseThrow();
        assertThat(updatedPenalty.getStatus()).isEqualTo(PenaltyStatus.PAID);
    }

    @Test
    void testPayPenalty_ByNonMember_Fail() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("CARD");

        mockMvc.perform(post("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPayPenalty_AlreadyPaid_Fail() throws Exception {
        PayPenaltyRequest req1 = new PayPenaltyRequest();
        req1.setPenaltyId(penaltyId);
        req1.setMethod("CASH");
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk());

        PayPenaltyRequest req2 = new PayPenaltyRequest();
        req2.setPenaltyId(penaltyId);
        req2.setMethod("ONLINE");
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Penalty already paid"));
    }

    @Test
    void testPayPenalty_InvalidMethod_Fail() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("INVALID");

        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePayment() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("CASH");
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByPenaltyId(penaltyId).orElseThrow();
        PaymentDTO updateDto = PaymentDTO.builder()
                .amount(BigDecimal.valueOf(20000))
                .method("CARD")
                .paymentFor("PENALTY")
                .memberId(payment.getMember().getId())
                .build();

        mockMvc.perform(put("/api/payment/" + payment.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Payment updated = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updated.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(updated.getMethod()).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    void testUpdatePayment_ByNonAdmin_Fail() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("CASH");
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByPenaltyId(penaltyId).orElseThrow();
        PaymentDTO updateDto = PaymentDTO.builder()
                .amount(BigDecimal.valueOf(20000))
                .method("CARD")
                .paymentFor("PENALTY")
                .build();

        mockMvc.perform(put("/api/payment/" + payment.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPayment() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("CASH");
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByPenaltyId(penaltyId).orElseThrow();

        mockMvc.perform(get("/api/payment/" + payment.getId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(18000));
    }

    @Test
    void testGetPayment_ByNonOwner_Fail() throws Exception {
        PayPenaltyRequest req = new PayPenaltyRequest();
        req.setPenaltyId(penaltyId);
        req.setMethod("CASH");
        mockMvc.perform(post("/api/payment")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByPenaltyId(penaltyId).orElseThrow();

        mockMvc.perform(get("/api/payment/" + payment.getId()))
                .andExpect(status().isForbidden());
    }
}