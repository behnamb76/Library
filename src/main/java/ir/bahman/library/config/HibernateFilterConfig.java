package ir.bahman.library.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HibernateFilterConfig {
    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void enableSoftDeleteFilter() {
        entityManagerFactory.unwrap(Session.class)
                .enableFilter("deletedFilter")
                .setParameter("isDeleted", false);
    }
}
