package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import domain.organization.Department;
import org.springframework.stereotype.Component;
import persistence.jdbc.JdbcDepartmentRepository;

import java.util.List;
import java.util.Map;

@Component
public class DepartmentsAdapter implements ModelAdapter {
    private final JdbcDepartmentRepository repository;

    public DepartmentsAdapter(JdbcDepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list() {
        return repository.findAll().stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        Department department = new Department(
            AdapterSupport.requiredInt(body, "department_id"),
            AdapterSupport.requiredString(body, "name")
        );
        repository.save(department);
        return repository.findById(department.getId()).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Department department = new Department(id, AdapterSupport.requiredString(body, "name"));
        repository.save(department);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Department existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(existing);
        repository.deleteById(id);
        return deleted;
    }

    private Map<String, Object> toMap(Department department) {
        return AdapterSupport.toApiMap(department, Map.of("departmentId", "department_id"));
    }

    @Override
    public String modelName() { return "departments"; }

    @Override
    public String identityField() { return "department_id"; }

    @Override
    public List<String> writableFields() {
        return List.of("department_id", "name");
    }
}
