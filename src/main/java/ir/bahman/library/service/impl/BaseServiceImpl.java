package ir.bahman.library.service.impl;

import ir.bahman.library.model.base.BaseEntity;
import ir.bahman.library.service.BaseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

public abstract class BaseServiceImpl<T extends BaseEntity<ID>, ID extends Serializable> implements BaseService<T, ID> {
    private final JpaRepository<T, ID> repository;

    protected BaseServiceImpl(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public T persist(T t) {
        prePersist(t);
        T saved = repository.save(t);
        BeanUtils.copyProperties(t, saved);
        postPersist(saved);
        return saved;
    }

    @Override
    public abstract T update(ID id, T t);

    @Override
    @Transactional
    public void deleteById(ID id) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No entity " + id));

        preDelete(entity);

        try {
            Field deletedField = entity.getClass().getDeclaredField("deleted");
            deletedField.setAccessible(true);

            Class<?> type = deletedField.getType();
            if (type != boolean.class && type != Boolean.class) {
                throw new IllegalStateException("'deleted' field is not boolean");
            }

            deletedField.set(entity, true);
            repository.saveAndFlush(entity);

        } catch (NoSuchFieldException ex) {
            repository.delete(entity);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Unable to access 'deleted' field", ex);
        }

        postDelete(entity);
    }

    @Override
    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No entity " + id));
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    protected void prePersist(T t) {
    }

    protected void postPersist(T t) {
    }

    protected void preDelete(T t) {
    }

    protected void postDelete(T t) {
    }

}
