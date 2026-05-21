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

import domain.timesheet.Timesheet;
import domain.timesheet.TimesheetEntry;
import domain.timesheet.TimesheetStatus;
import repository.TimesheetRepository;

public class JdbcTimesheetRepository implements TimesheetRepository {
    private final Connection connection;

    public JdbcTimesheetRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Timesheet entity) {
        String sql = """
            INSERT INTO timesheets (timesheet_id, employee_id, project_id, period_start, period_end, status, rejection_reason)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                employee_id = VALUES(employee_id),
                project_id = VALUES(project_id),
                period_start = VALUES(period_start),
                period_end = VALUES(period_end),
                status = VALUES(status),
                rejection_reason = VALUES(rejection_reason)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            Object employee = ReflectionSupport.getField(entity, "employee");
            if (employee != null) {
                ps.setInt(2, (Integer) ReflectionSupport.getField(employee, "employeeId"));
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            Object project = ReflectionSupport.getField(entity, "project");
            if (project != null) {
                ps.setInt(3, (Integer) ReflectionSupport.getField(project, "projectId"));
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setDate(4, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "periodStart")));
            ps.setDate(5, Date.valueOf((java.time.LocalDate) ReflectionSupport.getField(entity, "periodEnd")));
            ps.setString(6, entity.getStatus().name());
            ps.setString(7, (String) ReflectionSupport.getField(entity, "rejectionReason"));
            ps.executeUpdate();
            saveEntries(entity);
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save timesheet", e);
        }
    }

    @Override
    public Optional<Timesheet> findById(Integer id) {
        String sql = "SELECT * FROM timesheets WHERE timesheet_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Timesheet timesheet = mapTimesheet(rs);
                ReflectionSupport.setField(timesheet, "entries", loadEntries(timesheet.getId()));
                return Optional.of(timesheet);
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to find timesheet by id", e);
        }
    }

    @Override
    public List<Timesheet> findAll() {
        String sql = "SELECT * FROM timesheets ORDER BY timesheet_id";
        List<Timesheet> timesheets = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timesheet timesheet = mapTimesheet(rs);
                ReflectionSupport.setField(timesheet, "entries", loadEntries(timesheet.getId()));
                timesheets.add(timesheet);
            }
            return timesheets;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load timesheets", e);
        }
    }

    @Override
    public List<Timesheet> findByProjectId(int projectId) {
        String sql = "SELECT * FROM timesheets WHERE project_id = ? ORDER BY timesheet_id";
        List<Timesheet> timesheets = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timesheet timesheet = mapTimesheet(rs);
                    ReflectionSupport.setField(timesheet, "entries", loadEntries(timesheet.getId()));
                    timesheets.add(timesheet);
                }
            }
            return timesheets;
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load timesheets by project", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        try (PreparedStatement ps1 = connection.prepareStatement("DELETE FROM timesheet_entries WHERE timesheet_id = ?");
             PreparedStatement ps2 = connection.prepareStatement("DELETE FROM timesheets WHERE timesheet_id = ?")) {
            ps1.setInt(1, id);
            ps1.executeUpdate();
            ps2.setInt(1, id);
            ps2.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to delete timesheet", e);
        }
    }

    private Timesheet mapTimesheet(ResultSet rs) throws SQLException {
        Timesheet timesheet = new Timesheet(
            rs.getInt("timesheet_id"),
            rs.getDate("period_start").toLocalDate(),
            rs.getDate("period_end").toLocalDate(),
            TimesheetStatus.Draft
        );
        ReflectionSupport.setField(timesheet, "status", TimesheetStatus.valueOf(rs.getString("status")));
        ReflectionSupport.setField(timesheet, "rejectionReason", rs.getString("rejection_reason"));
        return timesheet;
    }

    private List<TimesheetEntry> loadEntries(int timesheetId) throws SQLException {
        String sql = "SELECT * FROM timesheet_entries WHERE timesheet_id = ? ORDER BY entry_id";
        List<TimesheetEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, timesheetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(
                        new TimesheetEntry(
                            rs.getInt("entry_id"),
                            rs.getDate("work_date").toLocalDate(),
                            rs.getBigDecimal("hours"),
                            rs.getBoolean("billable"),
                            rs.getString("notes")
                        )
                    );
                }
            }
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    private void saveEntries(Timesheet timesheet) throws SQLException {
        List<TimesheetEntry> entries = (List<TimesheetEntry>) ReflectionSupport.getField(timesheet, "entries");
        String sql = """
            INSERT INTO timesheet_entries (entry_id, timesheet_id, work_date, hours, billable, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                timesheet_id = VALUES(timesheet_id),
                work_date = VALUES(work_date),
                hours = VALUES(hours),
                billable = VALUES(billable),
                notes = VALUES(notes)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (TimesheetEntry entry : entries) {
                ps.setInt(1, entry.getId());
                ps.setInt(2, timesheet.getId());
                ps.setDate(3, Date.valueOf(entry.getWorkDate()));
                ps.setBigDecimal(4, entry.getHours());
                ps.setBoolean(5, entry.isBillable());
                ps.setString(6, entry.getNotes());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
