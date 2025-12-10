package ir.bahman.library.Repository;

import ir.bahman.library.model.Book;
import ir.bahman.library.model.Reservation;
import ir.bahman.library.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByBookId(Long bookId);

    long countByBookId(Long bookId);

    @Query("""
    select count(r) > 0
    from Reservation r
    where r.member.id = :memberId
      and r.book.id = :bookId
      and r.status in ('ACTIVE', 'READY_FOR_PICKUP')
""")
    boolean existsActiveReservation(@Param("memberId") Long memberId,
                                    @Param("bookId") Long bookId);

    List<Reservation> findByBook_IdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Reservation r
    SET r.status = 'EXPIRED'
    WHERE r.status = 'AWAITING_PICKUP'
      AND r.expireDate < :now
      AND r.deleted = false
""")
    void expireReadyForPickupReservations(@Param("now") LocalDateTime now);

    @Query("""
    select count(l) > 0
    from Loan l
    where l.member.id = :memberId
      and l.bookCopy.book.id = :bookId
      and l.returnDate is null
""")
    boolean userHasLoanedBook(@Param("memberId") Long memberId,
                              @Param("bookId") Long bookId);

    @Query("""
    SELECT r
    FROM Reservation r
    WHERE r.book = :book
      AND r.status = 'ACTIVE'
      AND r.deleted = false
    ORDER BY r.queuePosition ASC
""")
    List<Reservation> findWaitingReservations(Book book);
}
