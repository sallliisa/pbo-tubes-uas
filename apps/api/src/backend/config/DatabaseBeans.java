package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import persistence.DatabaseConnection;
import repository.ClientRepository;
import repository.ContractRepository;
import repository.DepartmentRepository;
import repository.EmployeeRepository;
import repository.InvoiceRepository;
import repository.PositionRepository;
import repository.ProjectRepository;
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
    public EmployeeRepository employeeRepository(Connection connection) {
        return new JdbcEmployeeRepository(connection);
    }

    @Bean
    public ProjectRepository projectRepository(Connection connection) {
        return new JdbcProjectRepository(connection);
    }

    @Bean
    public ClientRepository clientRepository(Connection connection) {
        return new JdbcClientRepository(connection);
    }

    @Bean
    public DepartmentRepository departmentRepository(Connection connection) {
        return new JdbcDepartmentRepository(connection);
    }

    @Bean
    public PositionRepository positionRepository(Connection connection) {
        return new JdbcPositionRepository(connection);
    }

    @Bean
    public ContractRepository contractRepository(Connection connection) {
        return new JdbcContractRepository(connection);
    }

    @Bean
    public InvoiceRepository invoiceRepository(Connection connection) {
        return new JdbcInvoiceRepository(connection);
    }
}
