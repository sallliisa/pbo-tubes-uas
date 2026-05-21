package repository;

import java.util.List;

import domain.timesheet.Timesheet;

public interface TimesheetRepository extends Repository<Integer, Timesheet> {
    List<Timesheet> findByProjectId(int projectId);
}
