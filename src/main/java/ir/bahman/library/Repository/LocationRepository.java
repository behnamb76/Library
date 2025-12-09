package ir.bahman.library.Repository;

import ir.bahman.library.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsBySectionAndShelfAndRow(String section, String shelf, Integer row);

    @Query("""
        select l
        from Location l
        where l.section = :section
          and l.shelf = :shelf
          and l.row = :row
          and l.deleted = false
          and l.id <> :id
    """)
    boolean existsDuplicateOnUpdate(
            @Param("section") String section,
            @Param("shelf") String shelf,
            @Param("row") Integer row,
            @Param("id") Long id
    );
}