package service;

import java.math.BigDecimal;
import java.time.LocalDate;

import domain.billing.Contract;
import domain.billing.Invoice;
import domain.project.Project;
import domain.timesheet.Timesheet;
import repository.InvoiceRepository;

public class BillingService {
    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Contract createContract(
        Project project,
        int contractId,
        String title,
        LocalDate contractDate,
        String notes,
        String contractNumber,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal value,
        String terms
    ) {
        return project.addContract(
            contractId,
            title,
            contractDate,
            notes,
            contractNumber,
            startDate,
            endDate,
            value,
            terms
        );
    }

    public Invoice createInvoice(
        Project project,
        int invoiceId,
        String title,
        LocalDate invoiceDate,
        String notes,
        BigDecimal amount
    ) {
        Invoice invoice = new Invoice(invoiceId, title, invoiceDate, notes, amount);
        project.addInvoice(invoice);
        invoiceRepository.save(invoice);
        return invoice;
    }

    public void generateInvoiceFromTimesheet(Invoice invoice, Timesheet timesheet, BigDecimal rate) {
        invoice.generateFromTimesheet(timesheet, rate);
        invoiceRepository.save(invoice);
    }

    public void saveInvoice(Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    public void deleteInvoice(int invoiceId) {
        invoiceRepository.deleteById(invoiceId);
    }
}
