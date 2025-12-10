package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.*;
import ir.bahman.library.dto.AssignLocationRequest;
import ir.bahman.library.dto.BookCopyDTO;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.model.*;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.model.enums.BookCopyStatus;
import ir.bahman.library.model.enums.LoanStatus;
import ir.bahman.library.service.AuthService;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class BookCopyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    private String adminToken;
    private String librarianToken;
    private Long bookId;
    private Long locationId;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        Person admin = Person.builder()
                .firstName("Admin")
                .lastName("User")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .roles(List.of(adminRole, userRole))
                .deleted(false)
                .build();
        admin = personRepository.save(admin);
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
        adminToken = adminTokens.get("accessToken");


        RegisterRequest librarianReq = RegisterRequest.builder()
                .firstName("Lib")
                .lastName("Rarian")
                .nationalCode("0987654321")
                .phoneNumber("09876543210")
                .birthday(LocalDate.of(1990, 1, 1))
                .username("librarian")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/librarian-register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(librarianReq)))
                .andExpect(status().isCreated());

        Account librarianAccount = accountRepository.findByUsername("librarian").orElseThrow();
        librarianAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(librarianAccount);

        Map<String, String> libTokens = authService.login(new LoginRequest("librarian", "Pass1234"));
        librarianToken = libTokens.get("accessToken");

        RegisterRequest memberReq = RegisterRequest.builder()
                .firstName("Member")
                .lastName("User")
                .nationalCode("1111111111")
                .phoneNumber("09111111111")
                .birthday(LocalDate.of(1990, 1, 1))
                .username("memberuser")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/member-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReq)))
                .andExpect(status().isCreated());

        Account memberAccount = accountRepository.findByUsername("memberuser").orElseThrow();
        memberAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(memberAccount);

        Category category = categoryRepository.save(Category.builder()
                .name("Fiction")
                .deleted(false)
                .build());

        Book book = Book.builder()
                .title("Test Book")
                .author("Author")
                .isbn("1234567890123")
                .publisher("Publisher")
                .publicationYear(Year.of(2020))
                .replacementCost(BigDecimal.valueOf(100_000))
                .category(category)
                .deleted(false)
                .build();
        book = bookRepository.save(book);
        bookId = book.getId();

        Location location = Location.builder()
                .section("A")
                .shelf("1")
                .row(1)
                .deleted(false)
                .build();
        location = locationRepository.save(location);
        locationId = location.getId();
    }

    @AfterEach
    void tearDown() {
        penaltyRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookCopyRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        personRepository.deleteAllInBatch();
    }

    @Test
    void testCreateBookCopy() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/copy?bookId=" + bookId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.barcode").exists())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("barcode");
    }

    @Test
    void testCreateBookCopy_ByNonLibrarian_Fail() throws Exception {
        mockMvc.perform(post("/api/copy?bookId=" + bookId)
                        .header("Authorization", "Bearer " + "invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAssignLocationToBookCopy() throws Exception {
        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.AVAILABLE)
                .deleted(false)
                .build());

        AssignLocationRequest req = new AssignLocationRequest();
        req.setBookCopyId(copy.getId());
        req.setLocationId(locationId);

        mockMvc.perform(put("/api/copy/assign-location")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testAssignLocationToBookCopy_Fail() throws Exception {
        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.LOANED)
                .deleted(false)
                .build());

        AssignLocationRequest req = new AssignLocationRequest();
        req.setBookCopyId(copy.getId());
        req.setLocationId(locationId);

        mockMvc.perform(put("/api/copy/assign-location")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInspectReturnedBookCopy() throws Exception {
        Person member = personRepository.findByAccountUsername("memberuser").orElseThrow();

        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.RETURNED_PENDING_CHECK)
                .deleted(false)
                .build());
        bookCopyRepository.save(copy);

        Loan loan = Loan.builder()
                .loanDate(LocalDateTime.now().minusHours(24))
                .dueDate(LocalDateTime.now().minusHours(2))
                .returnDate(LocalDateTime.now())
                .status(LoanStatus.RETURNED)
                .member(member)
                .bookCopy(copy).build();
        loanRepository.save(loan);

        mockMvc.perform(put("/api/copy/inspect/" + copy.getId() + "?damaged=true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        BookCopy updated = bookCopyRepository.findById(copy.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookCopyStatus.DAMAGED);
    }

    @Test
    void testInspectReturnedBookCopy_Fail() throws Exception {
        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.AVAILABLE)
                .deleted(false)
                .build());

        mockMvc.perform(put("/api/copy/inspect/" + copy.getId() + "?damaged=false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkBookCopyAsLost() throws Exception {
        Person member = personRepository.findByAccountUsername("memberuser").orElseThrow();

        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.LOANED)
                .deleted(false)
                .build());
        bookCopyRepository.save(copy);

        Loan loan = Loan.builder()
                .loanDate(LocalDateTime.now().minusHours(24))
                .dueDate(LocalDateTime.now().minusHours(2))
                .status(LoanStatus.RETURNED)
                .member(member)
                .bookCopy(copy).build();
        loanRepository.save(loan);

        mockMvc.perform(put("/api/copy/mark-lost/" + copy.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        BookCopy updated = bookCopyRepository.findById(copy.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BookCopyStatus.LOST);
    }

    @Test
    void testMarkBookCopyAsLost_Fail() throws Exception {
        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.AVAILABLE)
                .deleted(false)
                .build());

        mockMvc.perform(put("/api/copy/mark-lost/" + copy.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateBookCopy() throws Exception {
        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.AVAILABLE)
                .barcode("OLD-BARCODE")
                .location(locationRepository.findById(locationId).orElseThrow())
                .deleted(false)
                .build());

        BookCopyDTO dto = BookCopyDTO.builder()
                .barcode("NEW-BARCODE")
                .status(BookCopyStatus.RESERVED)
                .bookId(bookId)
                .locationId(locationId)
                .build();

        mockMvc.perform(put("/api/copy/" + copy.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("NEW-BARCODE"));
    }

    @Test
    void testGetReturnedPendingCheckCopies() throws Exception {
        bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.RETURNED_PENDING_CHECK)
                .deleted(false)
                .build());

        bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.AVAILABLE)
                .deleted(false)
                .build());

        mockMvc.perform(get("/api/copy/get-pending-check")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetBookCopy() throws Exception {
        BookCopy copy = bookCopyRepository.save(BookCopy.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .status(BookCopyStatus.AVAILABLE)
                .barcode("TEST-BARCODE")
                .location(locationRepository.findById(locationId).orElseThrow())
                .deleted(false)
                .build());

        mockMvc.perform(get("/api/copy/" + copy.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").value("TEST-BARCODE"));
    }

    @Test
    void testGetBookCopy_Fail() throws Exception {
        mockMvc.perform(get("/api/copy/get-pending-check"))
                .andExpect(status().isForbidden());
    }
}