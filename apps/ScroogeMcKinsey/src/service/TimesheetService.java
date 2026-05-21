package service;

import java.math.BigDecimal;
import java.time.LocalDate;

import domain.organization.Employee;
import domain.project.Project;
import domain.timesheet.Timesheet;
import domain.timesheet.TimesheetEntry;

public class TimesheetService {
    public Timesheet createTimesheet(Employee employee, Project project, LocalDate periodStart, LocalDate periodEnd) {
        return employee.createTimesheet(project, periodStart, periodEnd);
    }

    public void addEntry(Timesheet timesheet, TimesheetEntry entry) {
        timesheet.addEntry(entry);
    }

    public void addEntry(
        Timesheet timesheet,
        int entryId,
        LocalDate workDate,
        BigDecimal hours,
        boolean billable,
        String notes
    ) {
        timesheet.addEntry(entryId, workDate, hours, billable, notes);
    }

    public void removeEntry(Timesheet timesheet, int entryId) {
        timesheet.removeEntry(entryId);
    }

    public void submit(Timesheet timesheet) {
        timesheet.submit();
    }

    public void approve(Timesheet timesheet) {
        timesheet.approve();
    }

    public void reject(Timesheet timesheet, String reason) {
        timesheet.reject(reason);
    }
}
