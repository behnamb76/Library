package ir.bahman.library.Repository;

import ir.bahman.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Loan l
    SET l.status = 'OVERDUE'
    WHERE l.status = 'ACTIVE'
      AND l.dueDate < :now
      AND l.returnDate IS NULL
      AND l.deleted = false
""")
    void markLoansAsOverdue(@Param("now") LocalDateTime now);

    @Query("""
        SELECT l
        FROM Loan l
        WHERE l.status = 'OVERDUE'
          AND l.deleted = false
    """)
    List<Loan> findAllOverdueLoans();

    Optional<Loan> findTopByBookCopyIdOrderByReturnDateDesc(Long bookCopyId);

    Optional<Loan> findByBookCopyIdOrderByLoanDateDesc(Long bookCopyId);
}
