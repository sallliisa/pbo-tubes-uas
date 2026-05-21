package persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import domain.organization.Department;
import repository.DepartmentRepository;

public class JdbcDepartmentRepository extends GenericJdbcRepository<Integer, Department> implements DepartmentRepository {

    public JdbcDepartmentRepository(Connection connection) {
        super(connection, "departments", "department_id", List.of("name"));
    }

    @Override
    protected Department mapResultSet(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("department_id"), rs.getString("name"));
    }

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Department entity) throws SQLException {
        ps.setInt(1, entity.getId());
        ps.setString(2, entity.getName());
    }
}
