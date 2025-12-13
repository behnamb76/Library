package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.BookCopyRepository;
import ir.bahman.library.Repository.BookRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.ReservationRepository;
import ir.bahman.library.exception.AccessDeniedException;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.model.enums.BookCopyStatus;
import ir.bahman.library.model.enums.ReservationStatus;
import ir.bahman.library.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl extends BaseServiceImpl<Reservation, Long> implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    public ReservationServiceImpl(JpaRepository<Reservation, Long> repository, ReservationRepository reservationRepository, PersonRepository personRepository, BookRepository bookRepository, BookCopyRepository bookCopyRepository) {
        super(repository);
        this.reservationRepository = reservationRepository;
        this.personRepository = personRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Override
    public Reservation reserveBook(Long bookId, Long memberId) {
        if (reservationRepository.existsActiveReservation(memberId, bookId)) {
            throw new AlreadyExistsException("You already have a reservation on this book");
        }

        if (reservationRepository.userHasLoanedBook(memberId, bookId)) {
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
    public void cancelReservation(Long id, String username) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found!"));

        Person requester = personRepository.findByAccountUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Person not found!"));

        boolean isOwner = reservation.getMember().getId().equals(requester.getId());
        boolean isStaff = requester.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("LIBRARIAN"));

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("You cannot cancel someone else's reservation.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setQueuePosition(null);
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

    @Scheduled(cron = "0 */10 * * * *")
    @Override
    public void expireReadyForPickupReservation() {
        LocalDateTime now = LocalDateTime.now();

        reservationRepository.expireReadyForPickupReservations(now);
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Override
    public void assignAvailableCopiesToReservations() {

        List<BookCopy> availableCopies =
                bookCopyRepository.findAvailableBookCopies();

        for (BookCopy copy : availableCopies) {

            List<Reservation> queue =
                    reservationRepository.findWaitingReservations(copy.getBook());

            if (queue.isEmpty()) {
                continue;
            }

            Reservation next = queue.get(0);

            copy.setStatus(BookCopyStatus.RESERVED);

            next.setStatus(ReservationStatus.AWAITING_PICKUP);
            next.setExpireDate(LocalDateTime.now().plusHours(24));

            reorderQueue(copy.getId());
        }
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
