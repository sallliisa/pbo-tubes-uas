package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import domain.organization.Client;
import domain.project.Project;
import domain.project.ProjectStatus;
import org.springframework.stereotype.Component;
import persistence.jdbc.JdbcClientRepository;
import persistence.jdbc.JdbcProjectRepository;

import java.util.List;
import java.util.Map;

@Component
public class ProjectsAdapter implements ModelAdapter {
    private final JdbcProjectRepository repository;
    private final JdbcClientRepository clientRepository;

    public ProjectsAdapter(JdbcProjectRepository repository, JdbcClientRepository clientRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Map<String, Object>> list() {
        List<Project> projects = repository.findAll();
        List<Map<String, Object>> rows = projects.stream().map(this::toMap).toList();
        return rows;
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, "project_id");
        Project project = fromBody(body, id);
        repository.save(project);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Project project = fromBody(body, id);
        repository.save(project);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Project existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(existing);
        repository.deleteById(id);
        return deleted;
    }

    private Project fromBody(Map<String, Object> body, int id) {
        String statusRaw = AdapterSupport.requiredString(body, "status");
        ProjectStatus status;
        try {
            status = ProjectStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            throw new ApiException("status must match ProjectStatus enum", 400);
        }

        Project project = new Project(
            id,
            AdapterSupport.requiredString(body, "name"),
            AdapterSupport.requiredString(body, "description"),
            AdapterSupport.requiredDate(body, "start_date"),
            AdapterSupport.requiredDate(body, "end_date"),
            status,
            AdapterSupport.requiredDecimal(body, "budget")
        );

        Integer clientId = AdapterSupport.optionalInt(body, "client_id");
        if (clientId != null) {
            Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ApiException("client_id references non-existent client", 400));
            project.setClient(client);
        }

        return project;
    }

    private Map<String, Object> toMap(Project project) {
        return AdapterSupport.toApiMap(project, Map.of(
            "projectId", "project_id",
            "clientId", "client_id",
            "startDate", "start_date",
            "endDate", "end_date"
        ));
    }

    @Override
    public String modelName() { return "projects"; }

    @Override
    public String identityField() { return "project_id"; }

    @Override
    public List<String> writableFields() {
        return List.of("project_id", "client_id", "name", "description", "start_date", "end_date", "status", "budget");
    }
}
