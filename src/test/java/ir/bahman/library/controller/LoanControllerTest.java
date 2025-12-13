package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.*;
import ir.bahman.library.dto.BorrowBookRequest;
import ir.bahman.library.dto.LoanUpdateRequest;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.ReturnBookRequest;
import ir.bahman.library.model.*;
import ir.bahman.library.model.enums.*;
import ir.bahman.library.security.JwtService;
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
class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

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
    private ReservationRepository reservationRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String librarianToken;
    private String memberToken;

    private Long memberId;
    private Long bookCopyId;

    @BeforeEach
    void setUp() {
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
        Map<String, String> adminLogin = authService.login(new LoginRequest("admin1", "admin1"));
        this.adminToken = adminLogin.get("accessToken");

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
        Map<String, String> libLogin = authService.login(new LoginRequest("librarian", "Pass1234"));
        this.librarianToken = libLogin.get("accessToken");

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
        Map<String, String> memberLogin = authService.login(new LoginRequest("member", "Pass1234"));
        this.memberToken = memberLogin.get("accessToken");
        this.memberId = member.getId();

        Category category = categoryRepository.save(Category.builder()
                .name("Fiction")
                .deleted(false)
                .build());
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
        this.bookCopyId = copy.getId();
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
    void testBorrowBook() throws Exception {
        BorrowBookRequest req = new BorrowBookRequest();
        req.setMemberId(memberId);
        req.setBookCopyId(bookCopyId);

        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.bookCopyId").value(bookCopyId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        BookCopy updated = bookCopyRepository.findById(bookCopyId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookCopyStatus.LOANED);
    }

    @Test
    void testReturnBook_OnTime() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        Loan loan = loanRepository.findByBookCopyIdOrderByLoanDateDesc(bookCopyId).orElseThrow();

        ReturnBookRequest returnReq = ReturnBookRequest.builder()
                .loanId(loan.getId())
                .memberId(memberId)
                .bookCopyId(bookCopyId).build();

        mockMvc.perform(put("/api/loan/return-book")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isOk());

        BookCopy updated = bookCopyRepository.findById(bookCopyId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookCopyStatus.RETURNED_PENDING_CHECK);

        assertThat(penaltyRepository.existsByLoan_Id(loan.getId())).isFalse();
    }

    @Test
    void testReturnBook_Overdue_ShouldCreatePenalty() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        Loan loan = loanRepository.findByBookCopyIdOrderByLoanDateDesc(bookCopyId).orElseThrow();
        loan.setDueDate(LocalDateTime.now().minusDays(2));
        loanRepository.save(loan);

        ReturnBookRequest returnReq = ReturnBookRequest.builder()
                .loanId(loan.getId())
                .memberId(memberId)
                .bookCopyId(bookCopyId).build();

        mockMvc.perform(put("/api/loan/return-book")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isOk());

        assertThat(penaltyRepository.existsByLoan_Id(loan.getId())).isTrue();
        Penalty penalty = penaltyRepository.findByLoanId(loan.getId()).orElseThrow();
        assertThat(penalty.getReason()).isEqualTo(PenaltyReason.OVERDUE);
        assertThat(penalty.getStatus()).isEqualTo(PenaltyStatus.UNPAID);
    }

    @Test
    void testUpdateLoan() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        Loan loan = loanRepository.findByBookCopyIdOrderByLoanDateDesc(bookCopyId).orElseThrow();

        LoanUpdateRequest updateReq = LoanUpdateRequest.builder()
                .loanDate(loan.getLoanDate())
                .dueDate(LocalDateTime.now().plusDays(30))
                .returnDate(null)
                .status(LoanStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/loan/" + loan.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateLoan_ByMember_Final() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        Loan loan = loanRepository.findByBookCopyIdOrderByLoanDateDesc(bookCopyId).orElseThrow();
        LoanUpdateRequest updateReq = LoanUpdateRequest.builder()
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(null)
                .status(LoanStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/loan/" + loan.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLoan_ByAdmin_ShouldSucceed() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        Loan loan = loanRepository.findByBookCopyIdOrderByLoanDateDesc(bookCopyId).orElseThrow();

        mockMvc.perform(get("/api/loan/" + loan.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId));
    }

    @Test
    void testCheckOverdueLoans() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        Loan loan = loanRepository.findByBookCopyIdOrderByLoanDateDesc(bookCopyId).orElseThrow();
        // Set dueDate to yesterday
        loan.setDueDate(LocalDateTime.now().minusDays(1));
        loanRepository.save(loan);

        mockMvc.perform(put("/api/loan/check-loans")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Loan updated = loanRepository.findById(loan.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    void testGetAllLoans() throws Exception {
        BorrowBookRequest borrowReq = new BorrowBookRequest();
        borrowReq.setMemberId(memberId);
        borrowReq.setBookCopyId(bookCopyId);
        mockMvc.perform(post("/api/loan/borrow-book")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/loan")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}