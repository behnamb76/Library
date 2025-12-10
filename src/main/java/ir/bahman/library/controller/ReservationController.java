package ir.bahman.library.controller;

import ir.bahman.library.dto.ReservationDTO;
import ir.bahman.library.mapper.ReservationMapper;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER')")
    @PostMapping
    public ResponseEntity<ReservationDTO> reserveBook(@Valid @RequestBody ReservationDTO dto) {
        Reservation reservation = reservationService.reserveBook(dto.getBookId(), dto.getMemberId());

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
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);

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
