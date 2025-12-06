package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.BookRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.ReservationRepository;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.model.enums.ReservationStatus;
import ir.bahman.library.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl extends BaseServiceImpl<Reservation, Long> implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;
    private final BookRepository bookRepository;

    public ReservationServiceImpl(JpaRepository<Reservation, Long> repository, ReservationRepository reservationRepository, PersonRepository personRepository, BookRepository bookRepository) {
        super(repository);
        this.reservationRepository = reservationRepository;
        this.personRepository = personRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public Reservation reserveBook(Long bookId, Long memberId) {
        if (reservationRepository.existsByBookId(bookId)) {
            throw new AlreadyExistsException("You already have a reservation on this book");
        }

        if (reservationRepository.existsByBookIdAndLoanIsContaining(bookId)) {
            throw new AlreadyExistsException("You already have this book on loan");
        }

        Person member = personRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found!"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found!"));

        long position = reservationRepository.countByBookId(bookId) + 1;

        Reservation reservation = Reservation.builder()
                .reserveDate(LocalDateTime.now())
                .queuePosition((int) position)
                .status(ReservationStatus.ACTIVE)
                .member(member)
                .book(book).build();

        return persist(reservation);
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found!"));

        reservation.setStatus(ReservationStatus.CANCELLED);

        update(id, reservation);
    }

    @Override
    public void reorderQueue(Long bookId) {
        List<Reservation> reservations = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.ACTIVE);

        int counter = 1;
        for (Reservation r : reservations) {
            r.setQueuePosition(counter++);
        }

        reservationRepository.saveAll(reservations);
    }

    @Override
    public void expireReadyForPickupReservation() {
        LocalDateTime now = LocalDateTime.now();

        reservationRepository.expireReadyForPickupReservations(now);
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found!"));

        existing.setReserveDate(reservation.getReserveDate());
        existing.setExpireDate(reservation.getExpireDate());
        existing.setQueuePosition(reservation.getQueuePosition());
        existing.setStatus(reservation.getStatus());

        return reservationRepository.save(existing);
    }
}
