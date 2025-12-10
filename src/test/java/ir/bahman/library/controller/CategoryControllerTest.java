package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.CategoryRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.dto.CategoryDTO;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Category;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;
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
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

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
    private String librarianToken;


    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role librarianRole = roleRepository.findByName("LIBRARIAN").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();

        Person admin = personRepository.save(
                ir.bahman.library.model.Person.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .nationalCode("1234567890")
                        .phoneNumber("09123456789")
                        .roles(List.of(adminRole, userRole))
                        .deleted(false)
                        .build()
        );
        Account adminAcc = Account.builder()
                .username("admin1")
                .password(passwordEncoder.encode("admin1"))
                .status(AccountStatus.ACTIVE)
                .person(admin)
                .activeRole(adminRole)
                .deleted(false)
                .build();
        accountRepository.save(adminAcc);

        Person lib = personRepository.save(
                ir.bahman.library.model.Person.builder()
                        .firstName("Lib")
                        .lastName("Rarian")
                        .nationalCode("0987654321")
                        .phoneNumber("09876543210")
                        .roles(List.of(librarianRole, userRole))
                        .deleted(false)
                        .build()
        );
        Account libAcc = Account.builder()
                .username("librarian")
                .password(passwordEncoder.encode("Pass1234"))
                .status(AccountStatus.ACTIVE)
                .person(lib)
                .activeRole(librarianRole)
                .deleted(false)
                .build();
        accountRepository.save(libAcc);

        Map<String, String> adminTokens = authService.login(new LoginRequest("admin1", "admin1"));
        this.adminToken = adminTokens.get("accessToken");

        Map<String, String> libTokens = authService.login(new LoginRequest("librarian", "Pass1234"));
        this.librarianToken = libTokens.get("accessToken");
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    void testCreateCategory() throws Exception {
        CategoryDTO dto = CategoryDTO.builder()
                .name("Science")
                .description("Scientific books")
                .build();

        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Science"))
                .andExpect(jsonPath("$.description").value("Scientific books"));
    }

    @Test
    void teatCreateCategory_ByLibrarian() throws Exception {
        CategoryDTO dto = CategoryDTO.builder()
                .name("History")
                .build();

        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("History"));
    }

    @Test
    void testCreateCategory_WithDuplicateName_Fail() throws Exception {
        CategoryDTO first = CategoryDTO.builder().name("Fiction").build();
        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        CategoryDTO second = CategoryDTO.builder().name("Fiction").build();
        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category with this name already exists"));
    }

    @Test
    void testCreateCategory_WithBlankName_Fail() throws Exception {
        CategoryDTO dto = CategoryDTO.builder().name("").build();

        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void testUpdateCategory() throws Exception {
        Category original = categoryRepository.save(
                Category.builder().name("Old Name").description("Old Desc").deleted(false).build()
        );

        CategoryDTO dto = CategoryDTO.builder()
                .name("New Name")
                .description("New Description")
                .build();

        mockMvc.perform(put("/api/category/" + original.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Category updated = categoryRepository.findById(original.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Description");
    }

    @Test
    void testUpdateCategory_WithDuplicateName_Fail() throws Exception {
        categoryRepository.save(Category.builder().name("Existing").deleted(false).build());
        Category toUpdate = categoryRepository.save(Category.builder().name("UpdateMe").deleted(false).build());

        CategoryDTO dto = CategoryDTO.builder().name("Existing").build();

        mockMvc.perform(put("/api/category/" + toUpdate.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category with this name already exists"));
    }

    @Test
    void testGetCategory() throws Exception {
        Category category = categoryRepository.save(
                Category.builder().name("Mystery").deleted(false).build()
        );

        mockMvc.perform(get("/api/category/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mystery"));
    }

    @Test
    void testGetCategory_NonExistent_Fail() throws Exception {
        mockMvc.perform(get("/api/category/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCategories() throws Exception {
        categoryRepository.save(Category.builder().name("Category1").deleted(false).build());
        categoryRepository.save(Category.builder().name("Category2").deleted(false).build());

        mockMvc.perform(get("/api/category")
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}