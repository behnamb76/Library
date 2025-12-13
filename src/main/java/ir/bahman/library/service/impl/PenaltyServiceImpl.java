package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.LoanRepository;
import ir.bahman.library.Repository.PenaltyRepository;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Penalty;
import ir.bahman.library.model.enums.PenaltyReason;
import ir.bahman.library.model.enums.PenaltyStatus;
import ir.bahman.library.service.PenaltyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PenaltyServiceImpl extends BaseServiceImpl<Penalty, Long> implements PenaltyService {
    private final PenaltyRepository penaltyRepository;
    private final LoanRepository loanRepository;

    @Value("${library.penalty.daily-fee:3000}")
    private int penaltyDailyFee;

    public PenaltyServiceImpl(JpaRepository<Penalty, Long> repository, PenaltyRepository penaltyRepository, LoanRepository loanRepository) {
        super(repository);
        this.penaltyRepository = penaltyRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public Penalty createPenaltyForReason(Long loanId, PenaltyReason reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found!"));

        if (penaltyRepository.existsByLoan_Id(loan.getId())) {
            throw new AlreadyExistsException("Already have penalty");
        }

        LocalDateTime penaltyDate = loan.getReturnDate();
        if (penaltyDate == null) {
            penaltyDate = LocalDateTime.now();
        }

        Penalty penalty = Penalty.builder()
                .amount(calculatePenalty(loan, penaltyDate))
                .reason(reason)
                .status(PenaltyStatus.UNPAID)
                .lastCalculatedAt(LocalDateTime.now())
                .loan(loan).build();

        return persist(penalty);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Override
    public void autoCreatePenaltiesForOverdueLoans() {
        LocalDateTime today = LocalDateTime.now();
        List<Loan> overdueLoans = loanRepository.findAllOverdueLoans();

        for (Loan loan : overdueLoans) {
            if (penaltyRepository.existsByLoan_Id(loan.getId())) {
                continue;
            }

            Penalty penalty = Penalty.builder()
                    .amount(calculatePenalty(loan, today))
                    .reason(PenaltyReason.OVERDUE)
                    .status(PenaltyStatus.UNPAID)
                    .lastCalculatedAt(today)
                    .loan(loan).build();

            persist(penalty);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Override
    public void incrementDailyPenalties() {
        LocalDateTime today = LocalDateTime.now();

        List<Penalty> penalties = penaltyRepository.findUnpaidOverduePenalties();

        for (Penalty penalty : penalties) {
            LocalDateTime last = penalty.getLastCalculatedAt();
            if (last != null && last.equals(today)) {
                continue;
            }

            penalty.setAmount(penalty.getAmount().add(BigDecimal.valueOf(penaltyDailyFee)));
            penalty.setLastCalculatedAt(today);
        }
    }

    @Override
    public void freezePenaltyForLoan(Long loanId) {
        penaltyRepository.findByLoanId(loanId)
                .filter(p -> p.getStatus() == PenaltyStatus.UNPAID)
                .ifPresent(p -> p.setLastCalculatedAt(LocalDateTime.now()));
    }

    @Override
    public List<Penalty> findPenaltyByMemberId(Long memberId) {
        return penaltyRepository.findByLoan_Member_Id(memberId);
    }

    @Override
    public Penalty update(Long id, Penalty penalty) {
        Penalty existing = penaltyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Penalty not found!"));

        existing.setAmount(penalty.getAmount());
        existing.setReason(penalty.getReason());
        existing.setStatus(penalty.getStatus());
        existing.setLastCalculatedAt(penalty.getLastCalculatedAt());

        return penaltyRepository.save(existing);
    }

    private BigDecimal calculatePenalty(Loan loan, LocalDateTime overdueDate) {
        long overdueDays = ChronoUnit.DAYS.between(
                loan.getDueDate(),
                overdueDate
        );

        BigDecimal dailyFine = BigDecimal.valueOf(penaltyDailyFee);
        long days = Math.max(overdueDays, 1);
        return dailyFine.multiply(BigDecimal.valueOf(days));
    }
}
