package persistence.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import domain.organization.Position;
import repository.PositionRepository;

public class JdbcPositionRepository extends GenericJdbcRepository<Integer, Position> implements PositionRepository {

    public JdbcPositionRepository(Connection connection) {
        super(connection, "positions", "position_id", List.of(
            "title", "level", "min_salary", "max_salary", "description"
        ));
    }

    @Override
    protected Position mapResultSet(ResultSet rs) throws SQLException {
        return new Position(
            rs.getInt("position_id"),
            rs.getString("title"),
            rs.getString("level"),
            rs.getBigDecimal("min_salary"),
            rs.getBigDecimal("max_salary"),
            rs.getString("description")
        );
    }

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Position entity) throws SQLException {
        ps.setInt(1, entity.getId());
        ps.setString(2, (String) ReflectionSupport.getField(entity, "title"));
        ps.setString(3, (String) ReflectionSupport.getField(entity, "level"));
        ps.setBigDecimal(4, (BigDecimal) ReflectionSupport.getField(entity, "minSalary"));
        ps.setBigDecimal(5, (BigDecimal) ReflectionSupport.getField(entity, "maxSalary"));
        ps.setString(6, (String) ReflectionSupport.getField(entity, "description"));
    }
}
