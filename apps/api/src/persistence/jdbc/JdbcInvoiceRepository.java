package persistence.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import domain.billing.Invoice;
import domain.billing.InvoiceStatus;
import repository.InvoiceRepository;

public class JdbcInvoiceRepository extends GenericJdbcRepository<Integer, Invoice> implements InvoiceRepository {

    public JdbcInvoiceRepository(Connection connection) {
        super(connection, "invoices", "invoice_id", List.of(
            "project_id", "title", "invoice_date", "status", "notes", "amount", "signed", "signed_by", "signed_at"
        ));
    }

    @Override
    public List<Invoice> findByProjectId(int projectId) {
        return queryList("SELECT * FROM invoices WHERE project_id = ? ORDER BY invoice_id", projectId);
    }

    @Override
    protected Invoice mapResultSet(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice(
            rs.getInt("invoice_id"),
            rs.getString("title"),
            rs.getDate("invoice_date").toLocalDate(),
            rs.getString("notes"),
            rs.getBigDecimal("amount")
        );
        Object rawProjectId = rs.getObject("project_id");
        if (rawProjectId != null) {
            invoice.attachProject(((Number) rawProjectId).intValue());
        }
        ReflectionSupport.setField(invoice, "status", InvoiceStatus.valueOf(rs.getString("status")));
        ReflectionSupport.setField(invoice, "signed", rs.getBoolean("signed"));
        ReflectionSupport.setField(invoice, "signedBy", rs.getString("signed_by"));
        Date signedAt = rs.getDate("signed_at");
        ReflectionSupport.setField(invoice, "signedAt", signedAt != null ? signedAt.toLocalDate() : null);
        return invoice;
    }

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Invoice entity) throws SQLException {
        ps.setInt(1, entity.getId());
        if (entity.getProjectId() != null) {
            ps.setInt(2, entity.getProjectId());
        } else {
            ps.setNull(2, java.sql.Types.INTEGER);
        }
        ps.setString(3, (String) ReflectionSupport.getField(entity, "title"));
        ps.setDate(4, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "invoiceDate")));
        ps.setString(5, ((InvoiceStatus) ReflectionSupport.getField(entity, "status")).name());
        ps.setString(6, entity.getNotes());
        ps.setBigDecimal(7, entity.getAmount());
        ps.setBoolean(8, entity.isSigned());
        ps.setString(9, (String) ReflectionSupport.getField(entity, "signedBy"));
        Object signedAt = ReflectionSupport.getField(entity, "signedAt");
        if (signedAt != null) {
            ps.setDate(10, Date.valueOf((java.time.LocalDate) signedAt));
        } else {
            ps.setNull(10, java.sql.Types.DATE);
        }
    }
}
