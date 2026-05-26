package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import domain.organization.Position;
import org.springframework.stereotype.Component;
import repository.PositionRepository;

import java.util.List;
import java.util.Map;

@Component
public class PositionsAdapter implements ModelAdapter {
    private final PositionRepository repository;

    public PositionsAdapter(PositionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list() {
        return repository.findAll().stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        int id = AdapterSupport.nextIntId(repository.findAll());
        Position position = fromBody(body, id);
        repository.save(position);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Position position = fromBody(body, id);
        repository.save(position);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Position existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(existing);
        repository.deleteById(id);
        return deleted;
    }

    private Position fromBody(Map<String, Object> body, int id) {
        return new Position(
            id,
            AdapterSupport.requiredString(body, "title"),
            AdapterSupport.requiredString(body, "level"),
            AdapterSupport.requiredDecimal(body, "min_salary"),
            AdapterSupport.requiredDecimal(body, "max_salary"),
            AdapterSupport.requiredString(body, "description")
        );
    }

    private Map<String, Object> toMap(Position position) {
        return AdapterSupport.toApiMap(position, Map.of(
            "positionId", "position_id",
            "minSalary", "min_salary",
            "maxSalary", "max_salary"
        ));
    }

    @Override
    public String modelName() { return "positions"; }

    @Override
    public String identityField() { return "position_id"; }

    @Override
    public List<String> writableFields() {
        return List.of("position_id", "title", "level", "min_salary", "max_salary", "description");
    }
}
