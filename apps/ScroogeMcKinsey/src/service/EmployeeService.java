package service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import domain.organization.Department;
import domain.organization.Employee;
import domain.organization.Position;
import repository.EmployeeRepository;

public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void registerEmployee(Employee employee, Department department, Position position) {
        employee.assignPosition(position);
        department.addEmployee(employee);
        employeeRepository.save(employee);
    }

    public Optional<Employee> findEmployee(int employeeId) {
        return employeeRepository.findById(employeeId);
    }

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public void assignDepartmentManager(Department department, Employee manager) {
        department.assignManager(manager);
    }

    public void removeEmployeeFromDepartment(Department department, int employeeId) {
        department.removeEmployee(employeeId);
        employeeRepository.deleteById(employeeId);
    }

    public void save(Employee employee) {
        employeeRepository.save(employee);
    }

    public void deleteById(int employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    public void copyEmployees(Collection<? extends Employee> source, Collection<? super Employee> target) {
        target.addAll(source);
    }
}
