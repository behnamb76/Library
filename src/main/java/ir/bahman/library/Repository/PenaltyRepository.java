package ir.bahman.library.Repository;

import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Penalty;
import ir.bahman.library.model.enums.PenaltyReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    boolean existsByLoan_Id(Long loanId);

    @Query("""
        SELECT p
        FROM Penalty p
        WHERE p.status = 'UNPAID'
          AND p.loan.status = 'OVERDUE'
          AND p.deleted = false
    """)
    List<Penalty> findUnpaidOverduePenalties();

    Optional<Penalty> findByLoanId(Long loanId);

    boolean existsByLoanAndReason(Loan loan, PenaltyReason reason);

    Optional<Penalty> findByLoanAndReason(Loan loan, PenaltyReason reason);

    List<Penalty> findByLoan_Member_Id(Long loanMemberId);
}
