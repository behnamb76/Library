package ir.bahman.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.bahman.library.Repository.*;
import ir.bahman.library.dto.LoginRequest;
import ir.bahman.library.dto.ReservationDTO;
import ir.bahman.library.model.*;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.model.enums.BookCopyStatus;
import ir.bahman.library.model.enums.ReservationStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String librarianToken;
    private String memberToken;
    private Long memberId;
    private Long bookId;
    private Long bookCopyId;

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
        bookId = book.getId();

        BookCopy copy = BookCopy.builder()
                .book(book)
                .status(BookCopyStatus.AVAILABLE)
                .deleted(false)
                .build();
        copy = bookCopyRepository.save(copy);
        bookCopyId = copy.getId();
    }

    @AfterEach
    void tearDown() {
        loanRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        bookCopyRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        personRepository.deleteAllInBatch();
    }

    @Test
    void testReserveBook() throws Exception {
        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.queuePosition").value(1));

        Optional<Reservation> reservation = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE).stream().findFirst();
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getQueuePosition()).isEqualTo(1);
    }

    @Test
    void testReserveBook_WhenAlreadyReserved_Fail() throws Exception {
        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You already have a reservation on this book"));
    }

    @Test
    void testReserveBook_WhenAlreadyOnLoan_Fail() throws Exception {
        Loan loan = Loan.builder()
                .bookCopy(bookCopyRepository.findById(bookCopyId).orElseThrow())
                .member(personRepository.findById(memberId).orElseThrow())
                .loanDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(9))
                .status(ir.bahman.library.model.enums.LoanStatus.ACTIVE)
                .deleted(false)
                .build();
        loanRepository.save(loan);

        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You already have this book on loan"));
    }

    @Test
    void testCancelReservation() throws Exception {
        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE).get(0);

        mockMvc.perform(put("/api/reservation/cancel/" + reservation.getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        Reservation cancelled = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(cancelled.getQueuePosition()).isNull();
    }

    @Test
    void testReorderQueue() throws Exception {
        Person member2 = personRepository.save(Person.builder()
                .firstName("Member2")
                .lastName("User2")
                .nationalCode("4444444444")
                .phoneNumber("09444444444")
                .roles(List.of(roleRepository.findByName("MEMBER").orElseThrow(), roleRepository.findByName("USER").orElseThrow()))
                .deleted(false)
                .build());
        Account acc2 = Account.builder()
                .username("member2")
                .password(passwordEncoder.encode("Pass1234"))
                .status(AccountStatus.ACTIVE)
                .person(member2)
                .activeRole(roleRepository.findByName("MEMBER").orElseThrow())
                .deleted(false)
                .build();
        accountRepository.save(acc2);
        Map<String, String> tok2 = authService.login(new LoginRequest("member2", "Pass1234"));

        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + member2.getId())
                        .header("Authorization", "Bearer " + tok2.get("accessToken")))
                .andExpect(status().isCreated());

        List<Reservation> reservations = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE);
        assertThat(reservations).hasSize(2);
        assertThat(reservations.get(0).getQueuePosition()).isEqualTo(1);
        assertThat(reservations.get(1).getQueuePosition()).isEqualTo(2);

        mockMvc.perform(put("/api/reservation/cancel/" + reservations.get(0).getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/reservation/reorder_queue/" + bookId)
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk());

        List<Reservation> after = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE);
        assertThat(after).hasSize(1);
        assertThat(after.get(0).getQueuePosition()).isEqualTo(1);
    }

    @Test
    void testExpireReadyForPickupReservation() throws Exception {
        Reservation reservation = Reservation.builder()
                .book(bookRepository.findById(bookId).orElseThrow())
                .member(personRepository.findById(memberId).orElseThrow())
                .reserveDate(LocalDateTime.now().minusHours(25))
                .expireDate(LocalDateTime.now().minusHours(1))
                .queuePosition(1)
                .status(ReservationStatus.AWAITING_PICKUP)
                .deleted(false)
                .build();
        reservation = reservationRepository.save(reservation);

        mockMvc.perform(put("/api/reservation/expire/" + reservation.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void testGetReservation() throws Exception {
        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE).get(0);

        mockMvc.perform(get("/api/reservation/" + reservation.getId())
                        .header("Authorization", "Bearer " + librarianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId))
                .andExpect(jsonPath("$.memberId").value(memberId));
    }

    @Test
    void testUpdateReservation_ByNonLibrarian_Fail() throws Exception {
        mockMvc.perform(post("/api/reservation/reserve-book/" + bookId + "?memberId=" + memberId)
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE).get(0);
        ReservationDTO updateDto = ReservationDTO.builder()
                .reserveDate(reservation.getReserveDate())
                .expireDate(reservation.getExpireDate())
                .queuePosition(99)
                .memberId(memberId)
                .bookId(bookId)
                .build();

        mockMvc.perform(put("/api/reservation/" + reservation.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}