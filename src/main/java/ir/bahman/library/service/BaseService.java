package ir.bahman.library.service;

import ir.bahman.library.model.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T extends BaseEntity<ID>, ID extends Serializable> {
    T persist(T t);
    T update(ID id, T t);
    void deleteById(ID id);
    T findById(ID id);
    List<T> findAll();
}
