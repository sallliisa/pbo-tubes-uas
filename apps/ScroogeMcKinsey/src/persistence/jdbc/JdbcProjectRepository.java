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

import domain.organization.Client;
import domain.project.Project;
import domain.project.ProjectStatus;
import repository.ProjectRepository;

public class JdbcProjectRepository implements ProjectRepository {
    private final Connection connection;
    private final JdbcClientRepository clientRepository;

    public JdbcProjectRepository(Connection connection) {
        this.connection = connection;
        this.clientRepository = new JdbcClientRepository(connection);
    }

    @Override
    public void save(Project entity) {
        String sql = """
            INSERT INTO projects (project_id, client_id, name, description, start_date, end_date, status, budget)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                client_id = VALUES(client_id),
                name = VALUES(name),
                description = VALUES(description),
                start_date = VALUES(start_date),
                end_date = VALUES(end_date),
                status = VALUES(status),
                budget = VALUES(budget)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            Client client = (Client) ReflectionSupport.getField(entity, "client");
            if (client != null) {
                ps.setInt(2, client.getId());
            } else if (ReflectionSupport.getField(entity, "clientId") instanceof Integer clientId) {
                ps.setInt(2, clientId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, entity.getName());
            ps.setString(4, (String) ReflectionSupport.getField(entity, "description"));
            ps.setDate(5, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "startDate")));
            ps.setDate(6, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "endDate")));
            ps.setString(7, entity.getStatus().name());
            ps.setBigDecimal(8, (BigDecimal) ReflectionSupport.getField(entity, "budget"));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save project", e);
        }
    }

    @Override
    public Optional<Project> findById(Integer id) {
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapProject(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find project by id", e);
        }
    }

    @Override
    public Optional<Project> findByIdWithClient(int projectId) {
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapProject(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find project with client", e);
        }
    }

    @Override
    public List<Project> findAll() {
        String sql = "SELECT * FROM projects ORDER BY project_id";
        List<Project> projects = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                projects.add(mapProject(rs));
            }
            return projects;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load projects", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete project", e);
        }
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        Object rawClientId = rs.getObject("client_id");
        Project project = new Project(
            rs.getInt("project_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            ProjectStatus.valueOf(rs.getString("status")),
            rs.getBigDecimal("budget")
        );
        if (rawClientId != null) {
            int clientId = rs.getInt("client_id");
            ReflectionSupport.setField(project, "clientId", clientId);
            Optional<Client> client = clientRepository.findById(clientId);
            client.ifPresent(project::setClient);
        }
        return project;
    }
}
