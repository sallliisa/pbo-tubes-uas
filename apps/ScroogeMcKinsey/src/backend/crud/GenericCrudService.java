package backend.crud;

import backend.model.PaginatedResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GenericCrudService {
    private final CrudRegistry registry;

    public GenericCrudService(CrudRegistry registry) {
        this.registry = registry;
    }

    public PaginatedResult list(String model, PageRequest pageRequest) {
        ModelAdapter adapter = registry.resolve(model);
        List<Map<String, Object>> all = adapter.list();

        int total = all.size();
        int fromIndex = Math.min((pageRequest.page() - 1) * pageRequest.limit(), total);
        int toIndex = Math.min(fromIndex + pageRequest.limit(), total);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / pageRequest.limit());

        return new PaginatedResult(all.subList(fromIndex, toIndex), total, totalPages, pageRequest.page(), pageRequest.limit());
    }

    public Map<String, Object> create(String model, Map<String, Object> body) {
        return registry.resolve(model).create(body);
    }

    public Map<String, Object> update(String model, Map<String, Object> body) {
        return registry.resolve(model).update(body);
    }

    public Map<String, Object> delete(String model, Map<String, Object> body) {
        return registry.resolve(model).delete(body);
    }
}
