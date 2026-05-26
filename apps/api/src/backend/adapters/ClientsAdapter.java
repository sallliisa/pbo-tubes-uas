package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import domain.organization.Client;
import org.springframework.stereotype.Component;
import repository.ClientRepository;

import java.util.List;
import java.util.Map;

@Component
public class ClientsAdapter implements ModelAdapter {
    private final ClientRepository repository;

    public ClientsAdapter(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list() {
        return repository.findAll().stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        int id = AdapterSupport.nextIntId(repository.findAll());
        Client client = new Client(
            id,
            AdapterSupport.requiredString(body, "name"),
            AdapterSupport.requiredString(body, "industry"),
            AdapterSupport.requiredString(body, "contact_name"),
            AdapterSupport.requiredString(body, "contact_email"),
            AdapterSupport.requiredString(body, "contact_phone")
        );
        repository.save(client);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Client client = new Client(
            id,
            AdapterSupport.requiredString(body, "name"),
            AdapterSupport.requiredString(body, "industry"),
            AdapterSupport.requiredString(body, "contact_name"),
            AdapterSupport.requiredString(body, "contact_email"),
            AdapterSupport.requiredString(body, "contact_phone")
        );
        repository.save(client);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Client existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(existing);
        repository.deleteById(id);
        return deleted;
    }

    private Map<String, Object> toMap(Client client) {
        return AdapterSupport.toApiMap(client, Map.of(
            "clientId", "client_id",
            "contactName", "contact_name",
            "contactEmail", "contact_email",
            "contactPhone", "contact_phone"
        ));
    }

    @Override
    public String modelName() { return "clients"; }

    @Override
    public String identityField() { return "client_id"; }

    @Override
    public List<String> writableFields() {
        return List.of("client_id", "name", "industry", "contact_name", "contact_email", "contact_phone");
    }
}
