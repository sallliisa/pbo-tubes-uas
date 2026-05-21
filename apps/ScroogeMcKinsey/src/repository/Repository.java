package repository;

import java.util.List;
import java.util.Optional;

import domain.common.Identifiable;

public interface Repository<ID, T extends Identifiable<ID>> {
    void save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void deleteById(ID id);
}
