package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.LogoutRequest;
import ir.bahman.library.dto.RefreshRequest;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.security.JwtService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long memberId;

    @BeforeEach
    void setUp() throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        Role memberRole = roleRepository.findByName("MEMBER").orElseThrow();

        Person admin = Person.builder()
                .firstName("Admin")
                .lastName("User")
                .nationalCode("1234567890")
                .phoneNumber("09123456789")
                .birthday(LocalDate.of(1980, 1, 1))
                .roles(List.of(adminRole, userRole))
                .deleted(false)
                .build();
        admin = personRepository.save(admin);

        Account adminAccount = Account.builder()
                .username("admin1")
                .password(passwordEncoder.encode("admin1"))
                .status(AccountStatus.ACTIVE)
                .person(admin)
                .activeRole(adminRole)
                .deleted(false)
                .build();
        accountRepository.save(adminAccount);

        Person member = Person.builder()
                .firstName("Member")
                .lastName("User")
                .nationalCode("0987654321")
                .phoneNumber("09876543210")
                .birthday(LocalDate.of(1990, 1, 1))
                .roles(List.of(userRole, memberRole))
                .deleted(false)
                .build();
        member = personRepository.save(member);
        memberId = member.getId();

        Account memberAccount = Account.builder()
                .username("member")
                .password(passwordEncoder.encode("Pass1234"))
                .status(AccountStatus.PENDING)
                .person(member)
                .activeRole(userRole)
                .deleted(false)
                .build();
        accountRepository.save(memberAccount);
    }

    @AfterEach
    void tearDown(){
        accountRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest req = new LoginRequest("admin1", "admin1");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("accessToken").contains("refreshToken");

    }

    @Test
    void testLogin_WithInvalidPassword_Fail() throws Exception {
        LoginRequest req = new LoginRequest("admin1", "WrongPass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_WithNonExistentUser_Fail() throws Exception {
        LoginRequest req = new LoginRequest("ghost", "Pass1234");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_WithInactiveAccount_Fail() throws Exception {
        LoginRequest req = new LoginRequest("member", "Pass1234");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied. Your account is not active."));
    }

    @Test
    void testRefresh() throws Exception {
        LoginRequest loginReq = new LoginRequest("admin1", "admin1");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = extractToken(loginResult, "refreshToken");

        RefreshRequest refreshReq = new RefreshRequest(refreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String newRefreshToken = extractToken(refreshResult, "refreshToken");
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);
    }

    @Test
    void testLogout() throws Exception {
        LoginRequest loginReq = new LoginRequest("admin1", "admin1");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = extractToken(loginResult, "refreshToken");

        LogoutRequest logoutDto = new LogoutRequest(refreshToken);
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User admin1 logged out successfully"));

        Account account = accountRepository.findByUsername("admin1").orElseThrow();
        assertThat(account.getAuthId()).isNull();
    }

    @Test
    void testLogout_WithNoToken_Fail() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));
    }

    private String extractToken(MvcResult result, String field) throws Exception {
        String content = result.getResponse().getContentAsString();
        int start = content.indexOf("\"" + field + "\":\"") + ("\"" + field + "\":\"").length();
        int end = content.indexOf("\"", start);
        return content.substring(start, end);
    }
}