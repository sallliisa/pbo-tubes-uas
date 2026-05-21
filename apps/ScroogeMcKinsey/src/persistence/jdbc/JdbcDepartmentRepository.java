package persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.organization.Department;
import repository.DepartmentRepository;

public class JdbcDepartmentRepository implements DepartmentRepository {
    private final Connection connection;

    public JdbcDepartmentRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Department entity) {
        String sql = """
            INSERT INTO departments (department_id, name)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE name = VALUES(name)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            ps.setString(2, entity.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save department", e);
        }
    }

    @Override
    public Optional<Department> findById(Integer id) {
        String sql = "SELECT department_id, name FROM departments WHERE department_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapDepartment(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find department by id", e);
        }
    }

    @Override
    public List<Department> findAll() {
        String sql = "SELECT department_id, name FROM departments ORDER BY department_id";
        List<Department> departments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                departments.add(mapDepartment(rs));
            }
            return departments;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load departments", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM departments WHERE department_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete department", e);
        }
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("department_id"), rs.getString("name"));
    }
}
