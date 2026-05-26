package persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.common.Identifiable;
import repository.Repository;

public abstract class GenericJdbcRepository<ID, T extends Identifiable<ID>> implements Repository<ID, T> {
    protected final Connection connection;
    protected final String tableName;
    protected final String idColumn;
    protected final List<String> columns;
    private final String saveSql;

    protected GenericJdbcRepository(Connection connection, String tableName, String idColumn, List<String> columns) {
        this.connection = connection;
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.columns = columns;
        this.saveSql = buildSaveSql();
    }

    private String buildSaveSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (").append(idColumn);
        for (String col : columns) {
            sql.append(", ").append(col);
        }
        sql.append(") VALUES (?");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(", ?");
        }
        sql.append(") ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            sql.append(col).append(" = VALUES(").append(col).append(")");
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }
        return sql.toString();
    }

    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    protected abstract void bindSaveStatement(PreparedStatement ps, T entity) throws SQLException;

    protected String getSaveSql() {
        return this.saveSql;
    }

    @Override
    public void save(T entity) {
        String sql = getSaveSql();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindSaveStatement(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save entity to " + tableName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find entity by ID in " + tableName, e);
        }
    }

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName + " ORDER BY " + idColumn;
        List<T> entities = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                entities.add(mapResultSet(rs));
            }
            return entities;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load entities from " + tableName, e);
        }
    }

    @Override
    public void deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete entity from " + tableName, e);
        }
    }

    protected List<T> queryList(String sql, Object... params) {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to execute query on " + tableName, e);
        }
    }
}
