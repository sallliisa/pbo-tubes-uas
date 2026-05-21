package persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import domain.organization.Client;
import repository.ClientRepository;

public class JdbcClientRepository extends GenericJdbcRepository<Integer, Client> implements ClientRepository {

    public JdbcClientRepository(Connection connection) {
        super(connection, "clients", "client_id", List.of(
            "name", "industry", "contact_name", "contact_email", "contact_phone"
        ));
    }

    @Override
    protected Client mapResultSet(ResultSet rs) throws SQLException {
        return new Client(
            rs.getInt("client_id"),
            rs.getString("name"),
            rs.getString("industry"),
            rs.getString("contact_name"),
            rs.getString("contact_email"),
            rs.getString("contact_phone")
        );
    }

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Client entity) throws SQLException {
        ps.setInt(1, entity.getId());
        ps.setString(2, entity.getName());
        ps.setString(3, (String) ReflectionSupport.getField(entity, "industry"));
        ps.setString(4, (String) ReflectionSupport.getField(entity, "contactName"));
        ps.setString(5, (String) ReflectionSupport.getField(entity, "contactEmail"));
        ps.setString(6, (String) ReflectionSupport.getField(entity, "contactPhone"));
    }
}
