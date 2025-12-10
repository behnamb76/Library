package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.LocationRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.dto.LocationDTO;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Location;
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
class LocationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private LocationRepository locationRepository;

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
                .username("admin_loc")
                .password(passwordEncoder.encode("admin"))
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
                .username("librarian_loc")
                .password(passwordEncoder.encode("Pass1234"))
                .status(AccountStatus.ACTIVE)
                .person(lib)
                .activeRole(librarianRole)
                .deleted(false)
                .build();
        accountRepository.save(libAcc);

        Map<String, String> adminTokens = authService.login(new LoginRequest("admin_loc", "admin"));
        this.adminToken = adminTokens.get("accessToken");

        Map<String, String> libTokens = authService.login(new LoginRequest("librarian_loc", "Pass1234"));
        this.librarianToken = libTokens.get("accessToken");
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    void testCreateLocation() throws Exception {
        LocationDTO dto = LocationDTO.builder()
                .section("A")
                .shelf("1")
                .row(1)
                .build();

        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.section").value("A"))
                .andExpect(jsonPath("$.shelf").value("1"))
                .andExpect(jsonPath("$.row").value(1));

        assertThat(locationRepository.findBySectionAndShelfAndRow("A", "1", 1)).isPresent();
    }

    @Test
    void testCreateLocation_ByLibrarian() throws Exception {
        LocationDTO dto = LocationDTO.builder()
                .section("B")
                .shelf("2")
                .row(2)
                .build();

        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + librarianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.section").value("B"));
    }

    @Test
    void testCreateLocation_WithDuplicateSectionShelfRow_Fail() throws Exception {
        locationRepository.save(Location.builder()
                .section("C")
                .shelf("3")
                .row(3)
                .deleted(false)
                .build());

        LocationDTO duplicate = LocationDTO.builder()
                .section("C")
                .shelf("3")
                .row(3)
                .build();

        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Location already exists"));
    }

    @Test
    void testCreateLocation_WithMissingSection_Fail() throws Exception {
        LocationDTO dto = LocationDTO.builder()
                .section("")
                .shelf("1")
                .row(1)
                .build();

        mockMvc.perform(post("/api/location")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("section"));
    }

    @Test
    void testUpdateLocation() throws Exception {
        Location original = locationRepository.save(
                Location.builder().section("D").shelf("4").row(4).deleted(false).build()
        );

        LocationDTO updateDto = LocationDTO.builder()
                .section("D-Updated")
                .shelf("4-Updated")
                .row(44)
                .build();

        mockMvc.perform(put("/api/location/" + original.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Location updated = locationRepository.findById(original.getId()).orElseThrow();
        assertThat(updated.getSection()).isEqualTo("D-Updated");
        assertThat(updated.getShelf()).isEqualTo("4-Updated");
        assertThat(updated.getRow()).isEqualTo(44);
    }

    @Test
    void testUpdateLocation_WithDuplicate_Fail() throws Exception {
        locationRepository.save(Location.builder().section("E").shelf("5").row(5).deleted(false).build());
        Location toUpdate = locationRepository.save(Location.builder().section("F").shelf("6").row(6).deleted(false).build());

        LocationDTO dto = LocationDTO.builder()
                .section("E")
                .shelf("5")
                .row(5)
                .build();

        mockMvc.perform(put("/api/location/" + toUpdate.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Another location with same position exists"));
    }

    @Test
    void testGetLocation() throws Exception {
        Location location = locationRepository.save(
                Location.builder().section("G").shelf("7").row(7).deleted(false).build()
        );

        mockMvc.perform(get("/api/location/" + location.getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.section").value("G"))
                .andExpect(jsonPath("$.row").value(7));
    }

    @Test
    void testGetLocation_NonExistent_Fail() throws Exception {
        mockMvc.perform(get("/api/location/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}