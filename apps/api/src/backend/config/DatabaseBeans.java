package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import persistence.DatabaseConnection;
import persistence.jdbc.JdbcClientRepository;
import persistence.jdbc.JdbcContractRepository;
import persistence.jdbc.JdbcDepartmentRepository;
import persistence.jdbc.JdbcEmployeeRepository;
import persistence.jdbc.JdbcInvoiceRepository;
import persistence.jdbc.JdbcPositionRepository;
import persistence.jdbc.JdbcProjectRepository;

import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DatabaseBeans {
    @Bean(destroyMethod = "close")
    public Connection connection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    @Bean
    public JdbcEmployeeRepository employeeRepository(Connection connection) {
        return new JdbcEmployeeRepository(connection);
    }

    @Bean
    public JdbcProjectRepository projectRepository(Connection connection) {
        return new JdbcProjectRepository(connection);
    }

    @Bean
    public JdbcClientRepository clientRepository(Connection connection) {
        return new JdbcClientRepository(connection);
    }

    @Bean
    public JdbcDepartmentRepository departmentRepository(Connection connection) {
        return new JdbcDepartmentRepository(connection);
    }

    @Bean
    public JdbcPositionRepository positionRepository(Connection connection) {
        return new JdbcPositionRepository(connection);
    }

    @Bean
    public JdbcContractRepository contractRepository(Connection connection) {
        return new JdbcContractRepository(connection);
    }

    @Bean
    public JdbcInvoiceRepository invoiceRepository(Connection connection) {
        return new JdbcInvoiceRepository(connection);
    }
}
