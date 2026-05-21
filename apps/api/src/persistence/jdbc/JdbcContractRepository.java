package persistence.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import domain.billing.Contract;
import domain.billing.ContractStatus;
import repository.ContractRepository;

public class JdbcContractRepository extends GenericJdbcRepository<Integer, Contract> implements ContractRepository {

    public JdbcContractRepository(Connection connection) {
        super(connection, "contracts", "contract_id", List.of(
            "project_id", "title", "contract_date", "status", "notes", "contract_number", 
            "start_date", "end_date", "value", "terms", "signed", "signed_by", "signed_at"
        ));
    }

    @Override
    public List<Contract> findByProjectId(int projectId) {
        return queryList("SELECT * FROM contracts WHERE project_id = ? ORDER BY contract_id", projectId);
    }

    @Override
    protected Contract mapResultSet(ResultSet rs) throws SQLException {
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

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Contract entity) throws SQLException {
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
    }
}
