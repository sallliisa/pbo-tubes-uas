package repository;

import java.util.ArrayList;
import java.util.List;

import domain.billing.Invoice;

public class InMemoryInvoiceRepository extends InMemoryRepository<Integer, Invoice> implements InvoiceRepository {
    @Override
    public List<Invoice> findByProjectId(int projectId) {
        return new ArrayList<>(findAll());
    }
}
