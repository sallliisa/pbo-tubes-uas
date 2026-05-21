package persistence.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.organization.Position;
import repository.PositionRepository;

public class JdbcPositionRepository implements PositionRepository {
    private final Connection connection;

    public JdbcPositionRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Position entity) {
        String sql = """
            INSERT INTO positions (position_id, title, level, min_salary, max_salary, description)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                title = VALUES(title),
                level = VALUES(level),
                min_salary = VALUES(min_salary),
                max_salary = VALUES(max_salary),
                description = VALUES(description)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            ps.setString(2, (String) ReflectionSupport.getField(entity, "title"));
            ps.setString(3, (String) ReflectionSupport.getField(entity, "level"));
            ps.setBigDecimal(4, (BigDecimal) ReflectionSupport.getField(entity, "minSalary"));
            ps.setBigDecimal(5, (BigDecimal) ReflectionSupport.getField(entity, "maxSalary"));
            ps.setString(6, (String) ReflectionSupport.getField(entity, "description"));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save position", e);
        }
    }

    @Override
    public Optional<Position> findById(Integer id) {
        String sql = """
            SELECT position_id, title, level, min_salary, max_salary, description
            FROM positions
            WHERE position_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapPosition(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find position by id", e);
        }
    }

    @Override
    public List<Position> findAll() {
        String sql = "SELECT position_id, title, level, min_salary, max_salary, description FROM positions ORDER BY position_id";
        List<Position> positions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                positions.add(mapPosition(rs));
            }
            return positions;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load positions", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM positions WHERE position_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete position", e);
        }
    }

    private Position mapPosition(ResultSet rs) throws SQLException {
        return new Position(
            rs.getInt("position_id"),
            rs.getString("title"),
            rs.getString("level"),
            rs.getBigDecimal("min_salary"),
            rs.getBigDecimal("max_salary"),
            rs.getString("description")
        );
    }
}
