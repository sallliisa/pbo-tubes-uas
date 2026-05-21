package repository;

import java.util.List;

import domain.billing.Invoice;

public interface InvoiceRepository extends Repository<Integer, Invoice> {
    List<Invoice> findByProjectId(int projectId);
}
