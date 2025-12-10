package ir.bahman.library.Repository;

import ir.bahman.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    @Query("""
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.category c
            LEFT JOIN b.tags t
            WHERE
                LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                b.isbn LIKE CONCAT('%', :keyword, '%') OR
                LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Book> searchByKeyword(@Param("keyword") String keyword);

    Optional<Book> findByIsbn(String isbn);
}
