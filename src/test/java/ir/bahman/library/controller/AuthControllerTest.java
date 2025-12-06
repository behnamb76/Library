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
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.security.JwtService;
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
import java.util.List;

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
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest memberReq = RegisterRequest.builder()
                .firstName("Member")
                .lastName("User")
                .nationalCode("0987654321")
                .phoneNumber("09876543210")
                .birthday(LocalDate.of(1990, 1, 1))
                .username("memberuser")
                .password("Pass1234")
                .build();

        mockMvc.perform(post("/api/person/member-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReq)))
                .andExpect(status().isCreated());
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest req = new LoginRequest("admin", "admin");
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
        LoginRequest req = new LoginRequest("admin", "WrongPass");
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
        LoginRequest req = new LoginRequest("memberuser", "Pass1234");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied. Your account is not active."));
    }

    @Test
    void testRefresh() throws Exception {
        LoginRequest loginReq = new LoginRequest("admin", "admin");
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
    void testRefresh_WithInvalidToken_Fail() throws Exception {
        RefreshRequest req = new RefreshRequest("invalid.jwt.token");
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLogout() throws Exception {
        LoginRequest loginReq = new LoginRequest("admin", "admin");
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
                .andExpect(jsonPath("$.message").value("User admin logged out successfully"));

        Account account = accountRepository.findByUsername("admin").orElseThrow();
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