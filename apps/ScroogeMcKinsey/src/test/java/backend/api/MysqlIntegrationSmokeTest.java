package backend.api;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import persistence.DatabaseConnection;
import persistence.jdbc.JdbcClientRepository;
import persistence.jdbc.JdbcDepartmentRepository;
import persistence.jdbc.JdbcEmployeeRepository;
import persistence.jdbc.JdbcPositionRepository;
import persistence.jdbc.JdbcProjectRepository;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MysqlIntegrationSmokeTest {

    @Test
    void repositoriesCanQueryWhenMysqlTestsEnabled() throws Exception {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("SCROOGE_RUN_MYSQL_TESTS")));

        try (Connection connection = DatabaseConnection.getConnection()) {
            assertNotNull(connection);
            assertNotNull(new JdbcEmployeeRepository(connection).findAll());
            assertNotNull(new JdbcProjectRepository(connection).findAll());
            assertNotNull(new JdbcClientRepository(connection).findAll());
            assertNotNull(new JdbcDepartmentRepository(connection).findAll());
            assertNotNull(new JdbcPositionRepository(connection).findAll());
        }
    }
}
