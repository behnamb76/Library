package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.dto.ChangePasswordRequest;
import ir.bahman.library.dto.ChangeRoleRequest;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;
import ir.bahman.library.model.enums.AccountStatus;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String memberToken;
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

        memberAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(memberAccount);

        Map<String, String> adminTokens = authService.login(new LoginRequest("admin1", "admin1"));
        this.adminToken = adminTokens.get("accessToken");

        Map<String, String> memberTokens = authService.login(new LoginRequest("member", "Pass1234"));
        this.memberToken = memberTokens.get("accessToken");
    }

    @AfterEach
    void tearDown(){
        accountRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    void testActivateAccount() throws Exception {
        mockMvc.perform(put("/api/account/deactivate/" + memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/account/activate/" + memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testActivateAccount_ByNonAdmin_Fail() throws Exception {
        mockMvc.perform(put("/api/account/activate/" + memberId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeactivateAccount() throws Exception {
        mockMvc.perform(put("/api/account/deactivate/" + memberId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Account acc = accountRepository.findById(memberId).orElseThrow();
        assertThat(acc.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void testChangeRole() throws Exception {
        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRole("MEMBER");

        mockMvc.perform(put("/api/account/change-role")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Account updated = accountRepository.findByUsername("member").orElseThrow();
        assertThat(updated.getActiveRole().getName()).isEqualTo("MEMBER");
    }

    @Test
    void testChangeRole_UnassignedRole_Fail() throws Exception {
        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRole("ADMIN");

        mockMvc.perform(put("/api/account/change-role")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testChangePassword() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("Pass1234");
        req.setNewPassword("NewPass5678");

        mockMvc.perform(put("/api/account/change-password")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Map<String, String> newTokens = authService.login(new LoginRequest("member", "NewPass5678"));
        assertThat(newTokens).containsKey("accessToken");
    }

    @Test
    void changePassword_WrongOldPassword_Fail() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("WrongPass");
        req.setNewPassword("NewPass5678");

        mockMvc.perform(put("/api/account/change-password")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_NewPasswordSameAsOld_Fail() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("Pass1234");
        req.setNewPassword("Pass1234");

        mockMvc.perform(put("/api/account/change-password")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_InvalidNewPassword_Fail() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("Pass1234");
        req.setNewPassword("short");

        mockMvc.perform(put("/api/account/change-password")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}