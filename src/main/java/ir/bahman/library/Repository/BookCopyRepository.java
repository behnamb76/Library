package ir.bahman.library.Repository;

import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.enums.BookCopyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BookCopy c where c.id = :id")
    BookCopy lockById(@Param("id") Long id);

    List<BookCopy> findAllByStatus(BookCopyStatus status);

    @Query("""
    SELECT bc
    FROM BookCopy bc
    WHERE bc.status = 'AVAILABLE'
      AND bc.deleted = false
""")
    List<BookCopy> findAvailableBookCopies();
}
