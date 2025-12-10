package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.LibraryApplication;
import ir.bahman.library.Repository.*;
import ir.bahman.library.dto.BookDTO;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.model.*;
import ir.bahman.library.model.enums.AccountStatus;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        String username = "admin_" + System.nanoTime();
        Person admin = Person.builder()
                .firstName("Admin")
                .lastName("Test")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .roles(List.of(adminRole, userRole))
                .deleted(false)
                .build();
        admin = personRepository.save(admin);

        Account adminAcc = Account.builder()
                .username(username)
                .password(passwordEncoder.encode("adminPass123!"))
                .status(AccountStatus.ACTIVE)
                .person(admin)
                .activeRole(adminRole)
                .deleted(false)
                .build();
        accountRepository.save(adminAcc);

        Map<String, String> tokens = authService.login(new LoginRequest(username, "adminPass123!"));
        this.adminToken = tokens.get("accessToken");
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    void testCreateBook() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Science").deleted(false).build()
        );

        BookDTO bookDto = BookDTO.builder()
                .title("Physics for Beginners")
                .author("John Smith")
                .isbn("978-3-16-148410-0")
                .publisher("Academic Press")
                .publicationYear(Year.of(2020))
                .categoryName(category.getName())
                .replacementCost(BigDecimal.valueOf(150_000))
                .build();

        MvcResult result = mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Physics for Beginners"))
                .andExpect(jsonPath("$.isbn").value("978-3-16-148410-0"))
                .andReturn();

        Book created = bookRepository.findByIsbn("978-3-16-148410-0").orElseThrow();
        assertThat(created.getTitle()).isEqualTo("Physics for Beginners");
    }

    @Test
    void testCreateBook_DuplicateIsbn_Fail() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Fiction").deleted(false).build()
        );

        BookDTO first = BookDTO.builder()
                .title("Unique Book")
                .author("Author A")
                .isbn("979-8-74-527482-4")
                .publisher("Pub1")
                .publicationYear(Year.of(2021))
                .categoryName(category.getName())
                .replacementCost(BigDecimal.valueOf(100_000))
                .build();

        mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        BookDTO second = BookDTO.builder()
                .title("Different Book")
                .author("Author B")
                .isbn("979-8-74-527482-4")
                .publisher("Pub2")
                .publicationYear(Year.of(2022))
                .categoryName(category.getName())
                .replacementCost(BigDecimal.valueOf(120_000))
                .build();

        mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This book already exists!"));
    }

    @Test
    void testUpdateBook() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("History").deleted(false).build()
        );

        Book book = Book.builder()
                .title("Old Title")
                .author("Old Author")
                .isbn("222-2-22-222222-2")
                .publisher("Old Pub")
                .publicationYear(Year.of(2019))
                .category(category)
                .replacementCost(BigDecimal.valueOf(90_000))
                .deleted(false)
                .build();
        book = bookRepository.save(book);

        BookDTO updateDto = BookDTO.builder()
                .title("New Title")
                .author("New Author")
                .isbn("222-2-22-222222-2") // same ISBN
                .publisher("New Pub")
                .publicationYear(Year.of(2023))
                .categoryName(category.getName())
                .replacementCost(BigDecimal.valueOf(180_000))
                .build();

        mockMvc.perform(put("/api/book/" + book.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("New Title");
    }

    @Test
    void testSearchBook() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Mystery").deleted(false).build()
        );

        Book book1 = Book.builder()
                .title("The Silent Patient")
                .author("Alex Michaelides")
                .isbn("333-3-33-333333-3")
                .publisher("Celadon Books")
                .publicationYear(Year.of(2019))
                .category(category)
                .deleted(false)
                .build();
        bookRepository.save(book1);

        Book book2 = Book.builder()
                .title("Gone Girl")
                .author("Gillian Flynn")
                .isbn("444-4-44-444444-4")
                .publisher("Crown Publishing")
                .publicationYear(Year.of(2012))
                .category(category)
                .deleted(false)
                .build();
        bookRepository.save(book2);

        mockMvc.perform(get("/api/book/search/silent")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("The Silent Patient"));

        mockMvc.perform(get("/api/book/search/Flynn")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("Gillian Flynn"));

        mockMvc.perform(get("/api/book/search/333-3-33-333333-3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("333-3-33-333333-3"));

        mockMvc.perform(get("/api/book/search/Mystery")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testSearchBook_ByNonAdmin_Fail() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Test").deleted(false).build()
        );

        BookDTO bookDto = BookDTO.builder()
                .title("Unauthorized Book")
                .author("Me")
                .isbn("555-5-55-555555-5")
                .publisher("Me Pub")
                .publicationYear(Year.of(2025))
                .categoryName(category.getName())
                .replacementCost(BigDecimal.valueOf(50_000))
                .build();

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAssignTagToBook() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Drama").deleted(false).build()
        );

        Book book = Book.builder()
                .title("Tag Test Book")
                .author("Tag Author")
                .isbn("888-8-88-888888-8")
                .publisher("Tag Pub")
                .publicationYear(Year.of(2022))
                .category(category)
                .replacementCost(BigDecimal.valueOf(50_000))
                .deleted(false)
                .build();
        book = bookRepository.save(book);

        mockMvc.perform(put("/api/book/" + book.getId() + "/tag")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("tag", "BestSeller"))
                .andExpect(status().isOk());

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();

        assertThat(updatedBook.getTags()).hasSize(1);
        assertThat(updatedBook.getTags().get(0).getName()).isEqualTo("BESTSELLER");
    }

    @Test
    void testAssignTagToBook_PreventDuplicates() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Comedy").deleted(false).build()
        );

        Book book = Book.builder()
                .title("Duplicate Tag Book")
                .author("Author D")
                .isbn("777-7-77-777777-7")
                .publisher("Pub D")
                .publicationYear(Year.of(2022))
                .category(category)
                .replacementCost(BigDecimal.valueOf(50_000))
                .deleted(false)
                .build();
        book = bookRepository.save(book);

        mockMvc.perform(put("/api/book/" + book.getId() + "/tag")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("tag", "Funny"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/book/" + book.getId() + "/tag")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("tag", "Funny"))
                .andExpect(status().isOk());

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();

        assertThat(updatedBook.getTags()).hasSize(1);
        assertThat(updatedBook.getTags().get(0).getName()).isEqualTo("FUNNY");
    }

    @Test
    void testAssignTagToBook_Unauthorized_Fail() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Horror").deleted(false).build()
        );
        Book book = bookRepository.save(Book.builder()
                .title("Protected Book")
                .isbn("666-6-66-666666-6")
                .category(category)
                .deleted(false)
                .build());

        mockMvc.perform(put("/api/book/" + book.getId() + "/tag")
                        .param("tag", "NewTag"))
                .andExpect(status().isForbidden());
    }
}