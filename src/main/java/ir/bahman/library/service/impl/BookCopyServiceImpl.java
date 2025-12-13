package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.BookCopyRepository;
import ir.bahman.library.Repository.LoanRepository;
import ir.bahman.library.Repository.LocationRepository;
import ir.bahman.library.Repository.PenaltyRepository;
import ir.bahman.library.exception.BadRequestException;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.exception.IllegalStateException;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Location;
import ir.bahman.library.model.Penalty;
import ir.bahman.library.model.enums.BookCopyStatus;
import ir.bahman.library.model.enums.LoanStatus;
import ir.bahman.library.model.enums.PenaltyReason;
import ir.bahman.library.model.enums.PenaltyStatus;
import ir.bahman.library.service.BookCopyService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookCopyServiceImpl extends BaseServiceImpl<BookCopy, Long> implements BookCopyService {
    private final BookCopyRepository bookCopyRepository;
    private final PenaltyRepository penaltyRepository;
    private final LoanRepository loanRepository;
    private final LocationRepository locationRepository;

    public BookCopyServiceImpl(JpaRepository<BookCopy, Long> repository, BookCopyRepository bookCopyRepository, PenaltyRepository penaltyRepository, LoanRepository loanRepository, LocationRepository locationRepository) {
        super(repository);
        this.bookCopyRepository = bookCopyRepository;
        this.penaltyRepository = penaltyRepository;
        this.loanRepository = loanRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public void prePersist(BookCopy bookCopy) {
        bookCopy.setStatus(BookCopyStatus.AVAILABLE);
        bookCopy.setBarcode(generateBarcode(bookCopy.getBook().getId()));
    }

    @Override
    public List<BookCopy> findReturnedPendingCheckCopies() {
        return bookCopyRepository.findAllByStatus(BookCopyStatus.RETURNED_PENDING_CHECK);
    }

    @Override
    public void inspectReturnedBookCopy(Long id, boolean damaged) {

        BookCopy copy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Copy not found!"));

        if (copy.getStatus() != BookCopyStatus.RETURNED_PENDING_CHECK) {
            throw new IllegalStateException("Book copy is not pending inspection");
        }

        Loan lastLoan = loanRepository.findTopByBookCopyIdOrderByReturnDateDesc(id)
                .orElseThrow(() -> new EntityNotFoundException("No loan found for this copy!"));

        if (damaged) {

            copy.setStatus(BookCopyStatus.DAMAGED);

            if (!penaltyRepository.existsByLoanAndReason(lastLoan, PenaltyReason.DAMAGED)) {

                Penalty penalty = Penalty.builder()
                        .loan(lastLoan)
                        .reason(PenaltyReason.DAMAGED)
                        .status(PenaltyStatus.UNPAID)
                        .amount(calculateDamageFee(copy))
                        .lastCalculatedAt(LocalDateTime.now())
                        .build();

                penaltyRepository.save(penalty);
            }

            if (penaltyRepository.existsByLoanAndReason(lastLoan, PenaltyReason.OVERDUE)) {

                Penalty penalty = penaltyRepository.findByLoanAndReason(lastLoan, PenaltyReason.OVERDUE)
                                .orElseThrow(() -> new EntityNotFoundException("Penalty not found!"));

                penalty.setReason(PenaltyReason.OVERDUE_DAMAGED);
                penalty.setAmount(penalty.getAmount().add(calculateDamageFee(copy)));
                penalty.setLastCalculatedAt(LocalDateTime.now());

                penaltyRepository.save(penalty);
            }

        } else {
            copy.setStatus(BookCopyStatus.AVAILABLE);
        }
    }

    @Override
    public void markBookCopyAsLost(Long id) {
        BookCopy copy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Copy not found!"));

        if (copy.getStatus() == BookCopyStatus.LOST) {
            throw new IllegalStateException("Book copy is already marked as LOST");
        }

        if (copy.getStatus() == BookCopyStatus.AVAILABLE) {
            throw new IllegalStateException("Available copy cannot be marked lost");
        }

        Loan lastLoan = loanRepository.findTopByBookCopyIdOrderByReturnDateDesc(id)
                .orElseThrow(() -> new EntityNotFoundException("No loan found for this copy!"));

        if (lastLoan.getReturnDate() == null) {
            lastLoan.setReturnDate(LocalDateTime.now());
            lastLoan.setStatus(LoanStatus.LOST);
        }

        copy.setStatus(BookCopyStatus.LOST);

        if (!penaltyRepository.existsByLoanAndReason(lastLoan, PenaltyReason.LOST)) {

            Penalty penalty = Penalty.builder()
                    .loan(lastLoan)
                    .reason(PenaltyReason.LOST)
                    .status(PenaltyStatus.UNPAID)
                    .amount(calculateLostFee(copy))
                    .lastCalculatedAt(LocalDateTime.now()).build();

            penaltyRepository.save(penalty);
        }
    }

    @Override
    public void assignLocation(Long bookCopyId, Long locationId) {

        BookCopy copy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new EntityNotFoundException("Copy not found!"));

        if (copy.getStatus() == BookCopyStatus.LOST) {
            throw new BadRequestException("Lost book cannot have a location");
        }

        if (copy.getStatus() == BookCopyStatus.LOANED) {
            throw new BadRequestException("Cannot relocate a loaned book");
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found!"));

        copy.setLocation(location);
    }

    @Override
    public BookCopy update(Long id, BookCopy bookCopy) {
        BookCopy existing = bookCopyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Copy not found!"));

        existing.setBarcode(bookCopy.getBarcode());
        existing.setStatus(bookCopy.getStatus());
        existing.setLocation(bookCopy.getLocation());

        return bookCopyRepository.save(existing);
    }

    private String generateBarcode(Long bookId) {
        return "BC-" + bookId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal calculateDamageFee(BookCopy copy) {
        return copy.getBook()
                .getReplacementCost()
                .multiply(BigDecimal.valueOf(0.3));
    }

    private BigDecimal calculateLostFee(BookCopy copy) {
        return copy.getBook().getReplacementCost();
    }
}
