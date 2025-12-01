package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.dto.AssignRoleRequest;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.PersonDTO;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.security.JwtService;
import ir.bahman.library.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonControllerTest {
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

    private String adminAccessToken;
    private String memberAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        Map<String, String> adminTokens = authService.login(new LoginRequest("admin", "admin"));
        adminAccessToken = adminTokens.get("accessToken");

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

        Map<String, String> memberTokens = authService.login(new LoginRequest("memberuser", "Pass1234"));
        memberAccessToken = memberTokens.get("accessToken");
    }

    @Test
    void testMemberRegister() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("Ali")
                .lastName("Rezaei")
                .nationalCode("2222222222")
                .phoneNumber("09222222222")
                .birthday(LocalDate.of(1995, 5, 5))
                .username("alirezaei")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/member-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ali"));
    }

    @Test
    void testMemberRegister_InvalidNationalCode_Fail() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("Bad")
                .lastName("User")
                .nationalCode("123")
                .phoneNumber("09123456789")
                .birthday(LocalDate.of(1990, 1, 1))
                .username("baduser")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/member-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testMemberRegister_Duplicate_Fail() throws Exception {
        RegisterRequest first = RegisterRequest.builder()
                .firstName("Dup")
                .lastName("One")
                .nationalCode("4444444444")
                .phoneNumber("09444444444")
                .birthday(LocalDate.of(1990, 1, 1))
                .username("dupone")
                .password("Pass1234")
                .build();
        mockMvc.perform(post("/api/person/member-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        RegisterRequest second = RegisterRequest.builder()
                .firstName("Dup")
                .lastName("Two")
                .nationalCode("4444444444")
                .phoneNumber("09444444444")
                .birthday(LocalDate.of(1992, 2, 2))
                .username("duptwo")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/member-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This person already exists!"));
    }

    @Test
    void testLibrarianRegister() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("Lib")
                .lastName("Rarian")
                .nationalCode("3333333333")
                .phoneNumber("09333333333")
                .birthday(LocalDate.of(1985, 3, 3))
                .username("librarian1")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/librarian-register")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Lib"));
    }

    @Test
    void testLibrarianRegister_WithoutAdminToken_Forbidden() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("NotAdmin")
                .lastName("User")
                .nationalCode("5555555555")
                .phoneNumber("09555555555")
                .birthday(LocalDate.of(1990, 1, 1))
                .username("notadmin")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/librarian-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAssignRoleToPerson() throws Exception {
        Person person = personRepository.findByAccountUsername("memberuser").orElseThrow();

        AssignRoleRequest req = new AssignRoleRequest();
        req.setPersonId(person.getId());
        req.setRole("librarian");

        mockMvc.perform(post("/api/person/add-role")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Person updated = personRepository.findById(person.getId()).orElseThrow();
        assertThat(updated.getRoles()).extracting("name").contains("LIBRARIAN");
    }

    @Test
    void testAssignRoleToPerson_InvalidRole_Fail() throws Exception {
        Person person = personRepository.findByAccountUsername("memberuser").orElseThrow();
        AssignRoleRequest req = new AssignRoleRequest();
        req.setPersonId(person.getId());
        req.setRole("NONEXISTENT");

        mockMvc.perform(post("/api/person/add-role")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found!"));
    }

    @Test
    void testUpdateProfile() throws Exception {
        Person person = personRepository.findByAccountUsername("memberuser").orElseThrow();
        PersonDTO dto = PersonDTO.builder()
                .firstName("UpdatedMember")
                .lastName(person.getLastName())
                .nationalCode(person.getNationalCode())
                .phoneNumber(person.getPhoneNumber())
                .birthday(person.getBirthday())
                .build();

        mockMvc.perform(put("/api/person/" + person.getId())
                        .header("Authorization", "Bearer " + memberAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Person updated = personRepository.findById(person.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("UpdatedMember");
    }

    @Test
    void testUpdateProfile_BlankFirstName_Fail() throws Exception {
        Person person = personRepository.findByAccountUsername("memberuser").orElseThrow();
        PersonDTO dto = PersonDTO.builder()
                .firstName("") // invalid
                .lastName(person.getLastName())
                .nationalCode(person.getNationalCode())
                .phoneNumber(person.getPhoneNumber())
                .birthday(person.getBirthday())
                .build();

        mockMvc.perform(put("/api/person/" + person.getId())
                        .header("Authorization", "Bearer " + memberAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testSearchPeople() throws Exception {
        mockMvc.perform(get("/api/person/search/Member")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Member"));
    }

    @Test
    void testSearchPeople_WithoutAdmin_Forbidden() throws Exception {
        mockMvc.perform(get("/api/person/search/any")
                        .header("Authorization", "Bearer " + memberAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPersonRoles() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/person/person-roles")
                        .header("Authorization", "Bearer " + memberAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("USER", "MEMBER");
    }
}