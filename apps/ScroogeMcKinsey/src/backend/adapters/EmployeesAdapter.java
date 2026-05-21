package backend.adapters;

import backend.crud.ModelAdapter;
import backend.model.ApiException;
import backend.util.FieldUtil;
import domain.organization.Department;
import domain.organization.ContractEmployee;
import domain.organization.Employee;
import domain.organization.Position;
import domain.organization.PermanentEmployee;
import org.springframework.stereotype.Component;
import persistence.jdbc.JdbcEmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class EmployeesAdapter implements ModelAdapter {
    private final JdbcEmployeeRepository repository;

    public EmployeesAdapter(JdbcEmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> list() {
        return repository.findAll().stream().map(this::toMap).toList();
    }

    @Override
    public Map<String, Object> create(Map<String, Object> body) {
        Employee employee = fromBody(body, false);
        repository.save(employee);
        return repository.findById(employee.getId()).map(this::toMap)
            .orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Employee employee = fromBody(body, true);
        repository.save(employee);
        return repository.findById(id).map(this::toMap).orElseThrow(() -> new ApiException("Record not found", 404));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> body) {
        int id = AdapterSupport.requiredInt(body, identityField());
        Employee current = repository.findById(id).orElseThrow(() -> new ApiException("Record not found", 404));
        Map<String, Object> deleted = toMap(current);
        repository.deleteById(id);
        return deleted;
    }

    private Employee fromBody(Map<String, Object> body, boolean update) {
        int employeeId = AdapterSupport.requiredInt(body, "employee_id");
        String firstName = AdapterSupport.requiredString(body, "first_name");
        String lastName = AdapterSupport.requiredString(body, "last_name");
        String email = AdapterSupport.requiredString(body, "email");
        LocalDate hireDate = AdapterSupport.requiredDate(body, "hire_date");
        BigDecimal salary = AdapterSupport.requiredDecimal(body, "salary");
        String employeeType = AdapterSupport.requiredString(body, "employee_type");

        if ("Permanent Employee".equals(employeeType)) {
            String benefitPlan = AdapterSupport.requiredString(body, "benefit_plan");
            int annualLeaveQuota = AdapterSupport.requiredInt(body, "annual_leave_quota");
            return new PermanentEmployee(employeeId, firstName, lastName, email, hireDate, salary, benefitPlan, annualLeaveQuota);
        }

        if ("Contract Employee".equals(employeeType)) {
            LocalDate contractStartDate = AdapterSupport.requiredDate(body, "contract_start_date");
            LocalDate contractEndDate = AdapterSupport.requiredDate(body, "contract_end_date");
            return new ContractEmployee(employeeId, firstName, lastName, email, hireDate, salary, contractStartDate, contractEndDate);
        }

        throw new ApiException("employee_type must be 'Permanent Employee' or 'Contract Employee'", 400);
    }

    private Map<String, Object> toMap(Employee employee) {
        Map<String, Object> out = AdapterSupport.toApiMap(employee, Map.of(
            "employeeId", "employee_id",
            "firstName", "first_name",
            "lastName", "last_name",
            "hireDate", "hire_date",
            "employeeType", "employee_type",
            "benefitPlan", "benefit_plan",
            "annualLeaveQuota", "annual_leave_quota",
            "contractStartDate", "contract_start_date",
            "contractEndDate", "contract_end_date"
        ));

        Object position = FieldUtil.getField(employee, "position");
        if (position instanceof Position employeePosition) {
            out.put("position_id", employeePosition.getId());
            out.put("position_title", employeePosition.getTitle());
            out.put("position_level", FieldUtil.getField(employeePosition, "level"));
            out.put("position_min_salary", FieldUtil.getField(employeePosition, "minSalary"));
            out.put("position_max_salary", FieldUtil.getField(employeePosition, "maxSalary"));
            out.put("position_description", FieldUtil.getField(employeePosition, "description"));
        } else {
            out.put("position_id", null);
            out.put("position_title", null);
            out.put("position_level", null);
            out.put("position_min_salary", null);
            out.put("position_max_salary", null);
            out.put("position_description", null);
        }

        Object department = FieldUtil.getField(employee, "department");
        if (department instanceof Department employeeDepartment) {
            out.put("department_id", employeeDepartment.getId());
            out.put("department_name", employeeDepartment.getName());
        } else {
            out.put("department_id", null);
            out.put("department_name", null);
        }

        List<?> assignments = (List<?>) FieldUtil.getField(employee, "assignments");
        List<?> timesheets = (List<?>) FieldUtil.getField(employee, "timesheets");
        out.put("assignment_count", assignments == null ? 0 : assignments.size());
        out.put("timesheet_count", timesheets == null ? 0 : timesheets.size());

        if (employee instanceof PermanentEmployee permanentEmployee) {
            out.put("benefit_plan", permanentEmployee.getBenefitPlan());
            out.put("annual_leave_quota", permanentEmployee.getAnnualLeaveQuota());
            out.put("contract_start_date", null);
            out.put("contract_end_date", null);
        } else {
            out.put("benefit_plan", null);
            out.put("annual_leave_quota", null);
            out.put("contract_start_date", FieldUtil.getField(employee, "contractStartDate"));
            out.put("contract_end_date", FieldUtil.getField(employee, "contractEndDate"));
        }

        return out;
    }

    @Override
    public String modelName() { return "employees"; }

    @Override
    public String identityField() { return "employee_id"; }

    @Override
    public List<String> writableFields() {
        return List.of(
            "employee_id", "first_name", "last_name", "email", "hire_date", "salary", "employee_type",
            "benefit_plan", "annual_leave_quota", "contract_start_date", "contract_end_date"
        );
    }
}
