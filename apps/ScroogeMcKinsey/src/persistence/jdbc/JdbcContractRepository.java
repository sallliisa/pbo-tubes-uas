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

import domain.billing.Contract;
import domain.billing.ContractStatus;
import repository.ContractRepository;

public class JdbcContractRepository implements ContractRepository {
    private final Connection connection;

    public JdbcContractRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Contract entity) {
        String sql = """
            INSERT INTO contracts
            (contract_id, project_id, title, contract_date, status, notes, contract_number, start_date, end_date, value, terms, signed, signed_by, signed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                project_id = VALUES(project_id),
                title = VALUES(title),
                contract_date = VALUES(contract_date),
                status = VALUES(status),
                notes = VALUES(notes),
                contract_number = VALUES(contract_number),
                start_date = VALUES(start_date),
                end_date = VALUES(end_date),
                value = VALUES(value),
                terms = VALUES(terms),
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
            ps.setDate(4, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "contractDate")));
            ps.setString(5, ((ContractStatus) ReflectionSupport.getField(entity, "status")).name());
            ps.setString(6, (String) ReflectionSupport.getField(entity, "notes"));
            ps.setString(7, (String) ReflectionSupport.getField(entity, "contractNumber"));
            ps.setDate(8, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "startDate")));
            ps.setDate(9, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "endDate")));
            ps.setBigDecimal(10, entity.getContractValue());
            ps.setString(11, (String) ReflectionSupport.getField(entity, "terms"));
            ps.setBoolean(12, entity.isSigned());
            ps.setString(13, (String) ReflectionSupport.getField(entity, "signedBy"));
            Object signedAt = ReflectionSupport.getField(entity, "signedAt");
            if (signedAt != null) {
                ps.setDate(14, Date.valueOf((java.time.LocalDate) signedAt));
            } else {
                ps.setNull(14, java.sql.Types.DATE);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save contract", e);
        }
    }

    @Override
    public Optional<Contract> findById(Integer id) {
        String sql = "SELECT * FROM contracts WHERE contract_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapContract(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find contract by id", e);
        }
    }

    @Override
    public List<Contract> findAll() {
        String sql = "SELECT * FROM contracts ORDER BY contract_id";
        List<Contract> contracts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                contracts.add(mapContract(rs));
            }
            return contracts;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load contracts", e);
        }
    }

    @Override
    public List<Contract> findByProjectId(int projectId) {
        String sql = "SELECT * FROM contracts WHERE project_id = ? ORDER BY contract_id";
        List<Contract> contracts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contracts.add(mapContract(rs));
                }
            }
            return contracts;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load contracts by project", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM contracts WHERE contract_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete contract", e);
        }
    }

    private Contract mapContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract(
            rs.getInt("contract_id"),
            rs.getString("title"),
            rs.getDate("contract_date").toLocalDate(),
            rs.getString("notes"),
            rs.getString("contract_number"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getBigDecimal("value"),
            rs.getString("terms")
        );
        contract.attachProject(rs.getInt("project_id"));
        ReflectionSupport.setField(contract, "status", ContractStatus.valueOf(rs.getString("status")));
        ReflectionSupport.setField(contract, "signed", rs.getBoolean("signed"));
        ReflectionSupport.setField(contract, "signedBy", rs.getString("signed_by"));
        Date signedAt = rs.getDate("signed_at");
        ReflectionSupport.setField(contract, "signedAt", signedAt != null ? signedAt.toLocalDate() : null);
        return contract;
    }
}
