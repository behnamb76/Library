package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.BookCopyRepository;
import ir.bahman.library.Repository.LoanRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.ReservationRepository;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.exception.NotAvailableException;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.model.enums.BookCopyStatus;
import ir.bahman.library.model.enums.LoanStatus;
import ir.bahman.library.model.enums.ReservationStatus;
import ir.bahman.library.service.LoanService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public class LoanServiceImpl extends BaseServiceImpl<Loan, Long> implements LoanService {
    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;

    public LoanServiceImpl(JpaRepository<Loan, Long> repository, LoanRepository loanRepository, BookCopyRepository bookCopyRepository, ReservationRepository reservationRepository, PersonRepository personRepository) {
        super(repository);
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.reservationRepository = reservationRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Loan borrowBook(Long memberId, Long copyId) {
        BookCopy bookCopy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("Copy not found!"));

        Person member = personRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found!"));

        bookCopyRepository.lockById(copyId);

        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new NotAvailableException("Copy not available!");
        }

        List<Reservation> reservations = reservationRepository.findByBook_IdAndStatusOrderByQueuePositionAsc(bookCopy.getBook().getId(), ReservationStatus.ACTIVE);

        if (!reservations.isEmpty()) {
            Reservation firstReserve = reservations.get(0);

            if (!firstReserve.getMember().getId().equals(memberId)) {
                throw new NotAvailableException("This copy have reservation queue");
            }

            firstReserve.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(firstReserve);
        }

        Loan loan = Loan.builder()
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .status(LoanStatus.ACTIVE)
                .member(member)
                .bookCopy(bookCopy).build();
        persist(loan);

        bookCopy.setStatus(BookCopyStatus.LOANED);
        bookCopyRepository.save(bookCopy);

        return loan;
    }

    @Override
    public Loan update(Long id, Loan loan) {
        return null;
    }
}
