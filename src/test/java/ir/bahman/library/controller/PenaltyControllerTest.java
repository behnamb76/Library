package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.*;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.PenaltyDTO;
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
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PenaltyControllerTest {
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
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String librarianToken;
    private String memberToken;
    private Long loanId;
    private Long memberId;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role librarianRole = roleRepository.findByName("LIBRARIAN").orElseThrow();
        Role memberRole = roleRepository.findByName("MEMBER").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        Person admin = personRepository.save(Person.builder()
                .firstName("Admin")
                .lastName("User")
                .nationalCode("1111111111")
                .phoneNumber("09111111111")
                .roles(List.of(adminRole, userRole))
                .deleted(false)
                .build());
        Account adminAcc = Account.builder()
                .username("admin1")
                .password(passwordEncoder.encode("admin1"))
                .status(AccountStatus.ACTIVE)
                .person(admin)
                .activeRole(adminRole)
                .deleted(false)
                .build();
        accountRepository.save(adminAcc);
        Map<String, String> adminTokens = authService.login(new LoginRequest("admin1", "admin1"));
        this.adminToken = adminTokens.get("accessToken");

        Person librarian = personRepository.save(Person.builder()
                .firstName("Lib")
                .lastName("Rarian")
                .nationalCode("2222222222")
                .phoneNumber("09222222222")
                .roles(List.of(librarianRole, userRole))
                .deleted(false)
                .build());
        Account libAcc = Account.builder()
                .username("librarian")
                .password(passwordEncoder.encode("Pass1234"))
                .status(AccountStatus.ACTIVE)
                .person(librarian)
                .activeRole(librarianRole)
                .deleted(false)
                .build();
        accountRepository.save(libAcc);
        Map<String, String> libTokens = authService.login(new LoginRequest("librarian", "Pass1234"));
        this.librarianToken = libTokens.get("accessToken");

        Person member = personRepository.save(Person.builder()
                .firstName("Member")
                .lastName("User")
                .nationalCode("3333333333")
                .phoneNumber("09333333333")
                .roles(List.of(memberRole, userRole))
                .deleted(false)
                .build());
        Account memberAcc = Account.builder()
                .username("member")
                .password(passwordEncoder.encode("Pass1234"))
                .status(AccountStatus.ACTIVE)
                .person(member)
                .activeRole(memberRole)
                .deleted(false)
                .build();
        accountRepository.save(memberAcc);
        Map<String, String> memberTokens = authService.login(new LoginRequest("member", "Pass1234"));
        memberToken = memberTokens.get("accessToken");
        memberId = member.getId();

        Category category = categoryRepository.save(Category.builder().name("Fiction").deleted(false).build());
        Book book = Book.builder()
                .title("Test Book")
                .author("Author")
                .isbn("1234567890123")
                .publisher("Pub")
                .publicationYear(Year.of(2020))
                .replacementCost(BigDecimal.valueOf(100_000))
                .category(category)
                .deleted(false)
                .build();
        book = bookRepository.save(book);
        BookCopy copy = BookCopy.builder()
                .book(book)
                .status(BookCopyStatus.AVAILABLE)
                .deleted(false)
                .build();
        copy = bookCopyRepository.save(copy);

        Loan loan = Loan.builder()
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6))
                .member(member)
                .bookCopy(copy)
                .status(LoanStatus.OVERDUE)
                .deleted(false)
                .build();
        loan = loanRepository.save(loan);
        this.loanId = loan.getId();
    }

    @AfterEach
    void tearDown() {
        penaltyRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookCopyRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        personRepository.deleteAllInBatch();
    }

    @Test
    void testCreatePenaltyByLoanIdAndReason() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("OVERDUE"))
                .andExpect(jsonPath("$.status").value("UNPAID"));

        Penalty penalty = penaltyRepository.findByLoanId(loanId).orElseThrow();
        assertThat(penalty.getAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void testCreatePenaltyByLoanIdAndReason_ByNonLibrarian_Fail() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreatePenalty_Duplicate_Fail() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Already have penalty"));
    }

    @Test
    void testAutoCreatePenaltiesForOverdueLoans() throws Exception {
        mockMvc.perform(post("/api/Penalty/create-overdue-penalties")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(penaltyRepository.findByLoanId(loanId)).isPresent();
    }

    @Test
    void testFreezePenaltyForLoan() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/Penalty/freeze-penalty/" + loanId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        Penalty penalty = penaltyRepository.findByLoanId(loanId).orElseThrow();
        assertThat(penalty.getLastCalculatedAt()).isNotNull();
    }

    @Test
    void testUpdatePenalty() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        Penalty penalty = penaltyRepository.findByLoanId(loanId).orElseThrow();
        PenaltyDTO updateDto = PenaltyDTO.builder()
                .amount(BigDecimal.valueOf(50000))
                .reason("DAMAGED")
                .status("PAID")
                .build();

        mockMvc.perform(put("/api/Penalty/" + penalty.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Penalty updated = penaltyRepository.findById(penalty.getId()).orElseThrow();
        assertThat(updated.getReason()).isEqualTo(PenaltyReason.DAMAGED);
        assertThat(updated.getStatus()).isEqualTo(PenaltyStatus.PAID);
    }

    @Test
    void testGetPenalty() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        Penalty penalty = penaltyRepository.findByLoanId(loanId).orElseThrow();

        mockMvc.perform(get("/api/Penalty/" + penalty.getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("OVERDUE"));
    }

    @Test
    void testGetPenaltiesByMemberId() throws Exception {
        mockMvc.perform(post("/api/Penalty/" + loanId + "?reason=OVERDUE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/Penalty/member/" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetPenaltiesByMemberId_NonMember_Fail() throws Exception {
        mockMvc.perform(get("/api/Penalty/member/" + memberId)
                        .header("Authorization", "Bearer " + "invalid"))
                .andExpect(status().isForbidden());
    }
}