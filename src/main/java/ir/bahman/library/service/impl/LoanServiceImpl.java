package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.*;
import ir.bahman.library.exception.*;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.model.enums.BookCopyStatus;
import ir.bahman.library.model.enums.LoanStatus;
import ir.bahman.library.model.enums.PenaltyReason;
import ir.bahman.library.model.enums.ReservationStatus;
import ir.bahman.library.service.LoanService;
import ir.bahman.library.service.PenaltyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LoanServiceImpl extends BaseServiceImpl<Loan, Long> implements LoanService {
    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;
    private final PenaltyService penaltyService;
    private final PenaltyRepository penaltyRepository;

    @Value("${library.loan.duration-days:14}")
    private int loanDurationDays;

    public LoanServiceImpl(JpaRepository<Loan, Long> repository, LoanRepository loanRepository, BookCopyRepository bookCopyRepository, ReservationRepository reservationRepository, PersonRepository personRepository, PenaltyService penaltyService, PenaltyRepository penaltyRepository) {
        super(repository);
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.reservationRepository = reservationRepository;
        this.personRepository = personRepository;
        this.penaltyService = penaltyService;
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public Loan borrowBook(Long memberId, Long copyId) {
        boolean hasPenalties = penaltyRepository.existsUnpaidPenaltyByMember(memberId);

        if (hasPenalties) {
            throw new AccessDeniedException("Access Denied: You have outstanding unpaid penalties.");
        }

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
                .dueDate(LocalDateTime.now().plusDays(loanDurationDays))
                .status(LoanStatus.ACTIVE)
                .member(member)
                .bookCopy(bookCopy).build();
        persist(loan);

        bookCopy.setStatus(BookCopyStatus.LOANED);
        bookCopyRepository.save(bookCopy);

        return loan;
    }

    @Override
    public void returnBook(Long loanId, Long memberId, Long bookCopyId) {
        penaltyService.freezePenaltyForLoan(loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found!"));

        if (!loan.getMember().getId().equals(memberId)) {
            throw new BadRequestException("This loan belongs to another member");
        }

        if (!loan.getBookCopy().getId().equals(bookCopyId)) {
            throw new BadRequestException("This loan is for another Book Copy");
        }

        if (loan.getReturnDate() != null) {
            throw new AlreadyExistsException("This book has already been returned.");
        }

        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.RETURNED);

        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            penaltyService.createPenaltyForReason(loan.getId(), PenaltyReason.OVERDUE);
        }

        BookCopy bookCopy = loan.getBookCopy();
        bookCopy.setStatus(BookCopyStatus.RETURNED_PENDING_CHECK);
        bookCopyRepository.save(bookCopy);

        loanRepository.save(loan);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Override
    public void checkOverdueLoans() {
        LocalDateTime now = LocalDateTime.now();
        loanRepository.markLoansAsOverdue(now);
    }

    @Override
    public Loan update(Long id, Loan loan) {
        Loan existing = findById(id);

        existing.setLoanDate(loan.getLoanDate());
        existing.setDueDate(loan.getDueDate());
        existing.setReturnDate(loan.getReturnDate());
        existing.setStatus(loan.getStatus());

        return loanRepository.save(existing);
    }
}
