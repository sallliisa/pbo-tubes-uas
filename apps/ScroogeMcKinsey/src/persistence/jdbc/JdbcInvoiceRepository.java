package persistence.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.billing.Invoice;
import domain.billing.InvoiceStatus;
import repository.InvoiceRepository;

public class JdbcInvoiceRepository implements InvoiceRepository {
    private final Connection connection;

    public JdbcInvoiceRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Invoice entity) {
        String sql = """
            INSERT INTO invoices (invoice_id, project_id, title, invoice_date, status, notes, amount, signed, signed_by, signed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                project_id = VALUES(project_id),
                title = VALUES(title),
                invoice_date = VALUES(invoice_date),
                status = VALUES(status),
                notes = VALUES(notes),
                amount = VALUES(amount),
                signed = VALUES(signed),
                signed_by = VALUES(signed_by),
                signed_at = VALUES(signed_at)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save invoice", e);
        }
    }

    @Override
    public Optional<Invoice> findById(Integer id) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapInvoice(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find invoice by id", e);
        }
    }

    @Override
    public List<Invoice> findAll() {
        String sql = "SELECT * FROM invoices ORDER BY invoice_id";
        List<Invoice> invoices = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                invoices.add(mapInvoice(rs));
            }
            return invoices;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load invoices", e);
        }
    }

    @Override
    public List<Invoice> findByProjectId(int projectId) {
        String sql = "SELECT * FROM invoices WHERE project_id = ? ORDER BY invoice_id";
        List<Invoice> invoices = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapInvoice(rs));
                }
            }
            return invoices;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load invoices by project", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM invoices WHERE invoice_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete invoice", e);
        }
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice(
            rs.getInt("invoice_id"),
            rs.getString("title"),
            rs.getDate("invoice_date").toLocalDate(),
            rs.getString("notes"),
            rs.getBigDecimal("amount")
        );
        ReflectionSupport.setField(invoice, "status", InvoiceStatus.valueOf(rs.getString("status")));
        ReflectionSupport.setField(invoice, "signed", rs.getBoolean("signed"));
        ReflectionSupport.setField(invoice, "signedBy", rs.getString("signed_by"));
        Date signedAt = rs.getDate("signed_at");
        ReflectionSupport.setField(invoice, "signedAt", signedAt != null ? signedAt.toLocalDate() : null);
        return invoice;
    }
}
