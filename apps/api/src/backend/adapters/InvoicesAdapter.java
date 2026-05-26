package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import backend.util.FieldUtil;
import domain.billing.Invoice;
import domain.billing.InvoiceStatus;
import org.springframework.stereotype.Component;
import repository.InvoiceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class InvoicesAdapter implements ModelAdapter {
    private final InvoiceRepository repository;

    public InvoicesAdapter(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list() {
        return repository.findAll().stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        int id = AdapterSupport.nextIntId(repository.findAll());
        Invoice invoice = fromBody(body, id);
        repository.save(invoice);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Invoice existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Invoice invoice = fromBodyForUpdate(body, existing);
        repository.save(invoice);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Invoice existing = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(existing);
        repository.deleteById(id);
        return deleted;
    }

    private Invoice fromBody(Map<String, Object> body, int id) {
        Invoice invoice = new Invoice(
            id,
            AdapterSupport.requiredString(body, "title"),
            AdapterSupport.requiredDate(body, "invoice_date"),
            AdapterSupport.requiredString(body, "notes"),
            AdapterSupport.requiredDecimal(body, "amount")
        );

        Integer projectId = AdapterSupport.optionalInt(body, "project_id");
        if (projectId != null) {
            invoice.attachProject(projectId);
        }

        Object statusRaw = body.get("status");
        if (statusRaw != null) {
            try {
                FieldUtil.setField(invoice, "status", InvoiceStatus.valueOf(String.valueOf(statusRaw)));
            } catch (IllegalArgumentException e) {
                throw new ApiException("status must match InvoiceStatus enum", 400);
            }
        }

        Object signedRaw = body.get("signed");
        if (signedRaw != null) {
            FieldUtil.setField(invoice, "signed", Boolean.parseBoolean(String.valueOf(signedRaw)));
        }

        FieldUtil.setField(invoice, "signedBy", body.get("signed_by") == null ? null : String.valueOf(body.get("signed_by")));

        Object signedAtRaw = body.get("signed_at");
        LocalDate signedAt = null;
        if (signedAtRaw != null && !String.valueOf(signedAtRaw).isBlank()) {
            try {
                signedAt = LocalDate.parse(String.valueOf(signedAtRaw));
            } catch (Exception e) {
                throw new ApiException("signed_at must be date format yyyy-MM-dd", 400);
            }
        }
        FieldUtil.setField(invoice, "signedAt", signedAt);

        return invoice;
    }

    private Invoice fromBodyForUpdate(Map<String, Object> body, Invoice existing) {
        Map<String, Object> merged = new java.util.LinkedHashMap<>();
        merged.put("title", FieldUtil.getField(existing, "title"));
        merged.put("invoice_date", FieldUtil.getField(existing, "invoiceDate"));
        merged.put("notes", existing.getNotes());
        merged.put("amount", existing.getAmount());
        merged.put("status", String.valueOf(FieldUtil.getField(existing, "status")));
        merged.put("project_id", existing.getProjectId());
        merged.put("signed", existing.isSigned());
        merged.put("signed_by", FieldUtil.getField(existing, "signedBy"));
        Object existingSignedAt = FieldUtil.getField(existing, "signedAt");
        merged.put("signed_at", existingSignedAt == null ? null : String.valueOf(existingSignedAt));
        merged.putAll(body);
        return fromBody(merged, existing.getId());
    }

    private Map<String, Object> toMap(Invoice invoice) {
        return AdapterSupport.toApiMap(invoice, Map.of(
            "invoiceId", "invoice_id",
            "projectId", "project_id",
            "invoiceDate", "invoice_date",
            "signedBy", "signed_by",
            "signedAt", "signed_at"
        ));
    }

    @Override
    public String modelName() { return "invoices"; }

    @Override
    public String identityField() { return "invoice_id"; }

    @Override
    public List<String> writableFields() {
        return List.of(
            "invoice_id", "project_id", "title", "invoice_date", "status", "notes", "amount", "signed", "signed_by", "signed_at"
        );
    }
}
