package ir.bahman.library.Repository;

import ir.bahman.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("""
        select count(c) > 0
        from Category c
        where lower(c.name) = lower(:name)
          and c.deleted = false
          and c.id <> :id
    """)
    boolean existsByNameIgnoreCaseAndIdNot(
            @Param("name") String name,
            @Param("id") Long id
    );
}
