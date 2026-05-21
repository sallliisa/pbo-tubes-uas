package persistence.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import domain.organization.Client;
import domain.project.Project;
import domain.project.ProjectStatus;
import repository.ProjectRepository;

public class JdbcProjectRepository extends GenericJdbcRepository<Integer, Project> implements ProjectRepository {
    private final JdbcClientRepository clientRepository;

    public JdbcProjectRepository(Connection connection) {
        super(connection, "projects", "project_id", List.of(
            "client_id", "name", "description", "start_date", "end_date", "status", "budget"
        ));
        this.clientRepository = new JdbcClientRepository(connection);
    }

    @Override
    public Optional<Project> findByIdWithClient(int projectId) {
        return findById(projectId);
    }

    @Override
    protected Project mapResultSet(ResultSet rs) throws SQLException {
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

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Project entity) throws SQLException {
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
        ps.setDate(5, java.sql.Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "startDate")));
        ps.setDate(6, java.sql.Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "endDate")));
        ps.setString(7, entity.getStatus().name());
        ps.setBigDecimal(8, (BigDecimal) ReflectionSupport.getField(entity, "budget"));
    }
}
