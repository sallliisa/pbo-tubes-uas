package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import backend.util.FieldUtil;
import domain.billing.Contract;
import domain.billing.ContractStatus;
import org.springframework.stereotype.Component;
import repository.ContractRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class ContractsAdapter implements ModelAdapter {
    private final ContractRepository repository;

    public ContractsAdapter(ContractRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list() {
        return repository.findAll().stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        int id = AdapterSupport.nextIntId(repository.findAll());
        Contract contract = fromBody(body, id);
        repository.save(contract);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Contract contract = fromBody(body, id);
        repository.save(contract);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Contract existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(existing);
        repository.deleteById(id);
        return deleted;
    }

    private Contract fromBody(Map<String, Object> body, int id) {
        String valueField = body.containsKey("value") ? "value" : "contract_value";
        Contract contract = new Contract(
            id,
            AdapterSupport.requiredString(body, "title"),
            AdapterSupport.requiredDate(body, "contract_date"),
            AdapterSupport.requiredString(body, "notes"),
            AdapterSupport.requiredString(body, "contract_number"),
            AdapterSupport.requiredDate(body, "start_date"),
            AdapterSupport.requiredDate(body, "end_date"),
            AdapterSupport.requiredDecimal(body, valueField),
            AdapterSupport.requiredString(body, "terms")
        );

        Integer projectId = AdapterSupport.optionalInt(body, "project_id");
        if (projectId != null) {
            contract.attachProject(projectId);
        }

        Object statusRaw = body.get("status");
        if (statusRaw != null) {
            try {
                FieldUtil.setField(contract, "status", ContractStatus.valueOf(String.valueOf(statusRaw)));
            } catch (IllegalArgumentException e) {
                throw new ApiException("status must match ContractStatus enum", 400);
            }
        }

        Object signedRaw = body.get("signed");
        if (signedRaw != null) {
            FieldUtil.setField(contract, "signed", Boolean.parseBoolean(String.valueOf(signedRaw)));
        }

        FieldUtil.setField(contract, "signedBy", body.get("signed_by") == null ? null : String.valueOf(body.get("signed_by")));

        Object signedAtRaw = body.get("signed_at");
        LocalDate signedAt = null;
        if (signedAtRaw != null && !String.valueOf(signedAtRaw).isBlank()) {
            try {
                signedAt = LocalDate.parse(String.valueOf(signedAtRaw));
            } catch (Exception e) {
                throw new ApiException("signed_at must be date format yyyy-MM-dd", 400);
            }
        }
        FieldUtil.setField(contract, "signedAt", signedAt);

        return contract;
    }

    private Map<String, Object> toMap(Contract contract) {
        return AdapterSupport.toApiMap(contract, Map.of(
            "contractId", "contract_id",
            "projectId", "project_id",
            "contractDate", "contract_date",
            "contractNumber", "contract_number",
            "startDate", "start_date",
            "endDate", "end_date",
            "contractValue", "value",
            "signedBy", "signed_by",
            "signedAt", "signed_at"
        ));
    }

    @Override
    public String modelName() { return "contracts"; }

    @Override
    public String identityField() { return "contract_id"; }

    @Override
    public List<String> writableFields() {
        return List.of(
            "contract_id", "project_id", "title", "contract_date", "status", "notes", "contract_number",
            "start_date", "end_date", "value", "contract_value", "terms", "signed", "signed_by", "signed_at"
        );
    }
}
