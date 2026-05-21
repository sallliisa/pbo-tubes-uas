package persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.organization.Client;
import repository.ClientRepository;

public class JdbcClientRepository implements ClientRepository {
    private final Connection connection;

    public JdbcClientRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Client entity) {
        String sql = """
            INSERT INTO clients (client_id, name, industry, contact_name, contact_email, contact_phone)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                industry = VALUES(industry),
                contact_name = VALUES(contact_name),
                contact_email = VALUES(contact_email),
                contact_phone = VALUES(contact_phone)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            ps.setString(2, entity.getName());
            ps.setString(3, (String) ReflectionSupport.getField(entity, "industry"));
            ps.setString(4, (String) ReflectionSupport.getField(entity, "contactName"));
            ps.setString(5, (String) ReflectionSupport.getField(entity, "contactEmail"));
            ps.setString(6, (String) ReflectionSupport.getField(entity, "contactPhone"));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save client", e);
        }
    }

    @Override
    public Optional<Client> findById(Integer id) {
        String sql = """
            SELECT client_id, name, industry, contact_name, contact_email, contact_phone
            FROM clients
            WHERE client_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapClient(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find client by id", e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT client_id, name, industry, contact_name, contact_email, contact_phone FROM clients ORDER BY client_id";
        List<Client> clients = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clients.add(mapClient(rs));
            }
            return clients;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load clients", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete client", e);
        }
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        return new Client(
            rs.getInt("client_id"),
            rs.getString("name"),
            rs.getString("industry"),
            rs.getString("contact_name"),
            rs.getString("contact_email"),
            rs.getString("contact_phone")
        );
    }
}
