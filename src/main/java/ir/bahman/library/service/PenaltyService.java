package ir.bahman.library.service;

import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Penalty;
import ir.bahman.library.model.enums.PenaltyReason;

import java.util.List;

public interface PenaltyService extends BaseService<Penalty, Long> {
    Penalty createPenaltyForReason(Long loanId, PenaltyReason reason);

    void autoCreatePenaltiesForOverdueLoans();

    void incrementDailyPenalties();

    void freezePenaltyForLoan(Long loanId);

    List<Penalty> findPenaltyByMemberId(Long memberId);
}
