package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import domain.organization.Employee;

public class PayrollService {
    public BigDecimal calculateCompensation(Employee employee) {
        return employee.calculateCompensation();
    }

    public BigDecimal calculateTotalCompensation(Collection<? extends Employee> employees) {
        BigDecimal total = BigDecimal.ZERO;
        for (Employee employee : employees) {
            total = total.add(employee.calculateCompensation());
        }
        return total;
    }

    public List<BigDecimal> calculateCompensations(Collection<? extends Employee> employees) {
        List<BigDecimal> compensations = new ArrayList<>();
        for (Employee employee : employees) {
            compensations.add(calculateCompensation(employee));
        }
        return compensations;
    }
}
