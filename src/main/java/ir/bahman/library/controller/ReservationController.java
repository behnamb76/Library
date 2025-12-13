package ir.bahman.library.controller;

import ir.bahman.library.dto.ReservationDTO;
import ir.bahman.library.exception.AccessDeniedException;
import ir.bahman.library.mapper.ReservationMapper;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.service.PersonService;
import ir.bahman.library.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;
    private final PersonService personService;

    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper, PersonService personService) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
        this.personService = personService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
    @PostMapping("/reserve-book/{bookId}")
    public ResponseEntity<ReservationDTO> reserveBook(@PathVariable Long bookId, @RequestParam(required = false) Long memberId, Principal principal, HttpServletRequest request) {
        Person currentUser = personService.findByUsername(principal.getName());
        boolean isStaff = request.isUserInRole("ADMIN") || request.isUserInRole("LIBRARIAN");

        Long targetMemberId;

        if (isStaff && memberId != null) {
            targetMemberId = memberId;
        } else {
            targetMemberId = currentUser.getId();
        }

        Reservation reservation = reservationService.reserveBook(bookId, targetMemberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toDto(reservation));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateReservation(@Valid @RequestBody ReservationDTO dto, @PathVariable Long id) {
        reservationService.update(id, reservationMapper.toEntity(dto));

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
    @PutMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, Principal principal) {
        reservationService.cancelReservation(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/reorder_queue/{id}")
    public ResponseEntity<Void> reorderQueue(@PathVariable Long id) {
        reservationService.reorderQueue(id);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/expire/{id}")
    public ResponseEntity<Void> expireReadyForPickupReservation(@PathVariable Long id) {
        reservationService.expireReadyForPickupReservation();

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        ReservationDTO dto = reservationMapper.toDto(reservation);

        return ResponseEntity.ok().body(dto);
    }
}
