package repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import domain.common.Identifiable;

public class InMemoryRepository<ID, T extends Identifiable<ID>> implements Repository<ID, T> {
    private final Map<ID, T> storage = new HashMap<>();

    @Override
    public void save(T entity) {
        storage.put(entity.getId(), entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public void deleteById(ID id) {
        storage.remove(id);
    }
}
