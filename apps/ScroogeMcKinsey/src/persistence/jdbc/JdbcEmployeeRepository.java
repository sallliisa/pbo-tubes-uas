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

import domain.organization.ContractEmployee;
import domain.organization.Employee;
import domain.organization.PermanentEmployee;
import repository.EmployeeRepository;

public class JdbcEmployeeRepository implements EmployeeRepository {
    private final Connection connection;

    public JdbcEmployeeRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Employee entity) {
        String baseSql = """
            INSERT INTO employees
            (employee_id, first_name, last_name, email, hire_date, salary, position_id, department_id, employee_type)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                first_name = VALUES(first_name),
                last_name = VALUES(last_name),
                email = VALUES(email),
                hire_date = VALUES(hire_date),
                salary = VALUES(salary),
                position_id = VALUES(position_id),
                department_id = VALUES(department_id),
                employee_type = VALUES(employee_type)
            """;
        try (PreparedStatement ps = connection.prepareStatement(baseSql)) {
            ps.setInt(1, entity.getId());
            ps.setString(2, (String) ReflectionSupport.getField(entity, "firstName"));
            ps.setString(3, (String) ReflectionSupport.getField(entity, "lastName"));
            ps.setString(4, entity.getEmail());
            ps.setDate(5, Date.valueOf(entity.getHireDate()));
            ps.setBigDecimal(6, entity.getSalary());

            Object position = ReflectionSupport.getField(entity, "position");
            Object department = ReflectionSupport.getField(entity, "department");
            if (position != null) {
                ps.setInt(7, (Integer) ReflectionSupport.getField(position, "positionId"));
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            if (department != null) {
                ps.setInt(8, (Integer) ReflectionSupport.getField(department, "departmentId"));
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setString(9, entity.getEmployeeType());
            ps.executeUpdate();
            saveSubtypeDetails(entity);
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save employee", e);
        }
    }

    @Override
    public Optional<Employee> findById(Integer id) {
        String sql = """
            SELECT e.*, ped.benefit_plan, ped.annual_leave_quota,
                   ced.contract_start_date, ced.contract_end_date
            FROM employees e
            LEFT JOIN permanent_employee_details ped ON ped.employee_id = e.employee_id
            LEFT JOIN contract_employee_details ced ON ced.employee_id = e.employee_id
            WHERE e.employee_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapEmployee(rs));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find employee by id", e);
        }
    }

    @Override
    public List<Employee> findAll() {
        String sql = """
            SELECT e.*, ped.benefit_plan, ped.annual_leave_quota,
                   ced.contract_start_date, ced.contract_end_date
            FROM employees e
            LEFT JOIN permanent_employee_details ped ON ped.employee_id = e.employee_id
            LEFT JOIN contract_employee_details ced ON ced.employee_id = e.employee_id
            ORDER BY e.employee_id
            """;
        List<Employee> employees = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                employees.add(mapEmployee(rs));
            }
            return employees;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load employees", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (PreparedStatement ps1 = connection.prepareStatement("DELETE FROM permanent_employee_details WHERE employee_id = ?");
             PreparedStatement ps2 = connection.prepareStatement("DELETE FROM contract_employee_details WHERE employee_id = ?");
             PreparedStatement ps3 = connection.prepareStatement("DELETE FROM employees WHERE employee_id = ?")) {
            ps1.setInt(1, id);
            ps1.executeUpdate();
            ps2.setInt(1, id);
            ps2.executeUpdate();
            ps3.setInt(1, id);
            ps3.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete employee", e);
        }
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        int id = rs.getInt("employee_id");
        String type = rs.getString("employee_type");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        java.time.LocalDate hireDate = rs.getDate("hire_date").toLocalDate();
        BigDecimal salary = rs.getBigDecimal("salary");

        if ("Permanent Employee".equals(type)) {
            return new PermanentEmployee(
                id,
                firstName,
                lastName,
                email,
                hireDate,
                salary,
                rs.getString("benefit_plan"),
                rs.getInt("annual_leave_quota")
            );
        }

        return new ContractEmployee(
            id,
            firstName,
            lastName,
            email,
            hireDate,
            salary,
            rs.getDate("contract_start_date").toLocalDate(),
            rs.getDate("contract_end_date").toLocalDate()
        );
    }

    private void saveSubtypeDetails(Employee entity) throws SQLException {
        if (entity instanceof PermanentEmployee permanent) {
            String sql = """
                INSERT INTO permanent_employee_details (employee_id, benefit_plan, annual_leave_quota)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    benefit_plan = VALUES(benefit_plan),
                    annual_leave_quota = VALUES(annual_leave_quota)
                """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, permanent.getId());
                ps.setString(2, permanent.getBenefitPlan());
                ps.setInt(3, permanent.getAnnualLeaveQuota());
                ps.executeUpdate();
            }
            return;
        }

        if (entity instanceof ContractEmployee contract) {
            String sql = """
                INSERT INTO contract_employee_details (employee_id, contract_start_date, contract_end_date)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    contract_start_date = VALUES(contract_start_date),
                    contract_end_date = VALUES(contract_end_date)
                """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, contract.getId());
                ps.setDate(2, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(contract, "contractStartDate")));
                ps.setDate(3, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(contract, "contractEndDate")));
                ps.executeUpdate();
            }
        }
    }
}
