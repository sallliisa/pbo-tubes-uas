package persistence.jdbc;

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

public class JdbcTimesheetRepository extends GenericJdbcRepository<Integer, Timesheet> implements TimesheetRepository {

    public JdbcTimesheetRepository(Connection connection) {
        super(connection, "timesheets", "timesheet_id", List.of(
            "employee_id", "project_id", "period_start", "period_end", "status", "rejection_reason"
        ));
    }

    @Override
    public void save(Timesheet entity) {
        super.save(entity);
        try {
            saveEntries(entity);
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to save timesheet entries", e);
        }
    }

    @Override
    public Optional<Timesheet> findById(Integer id) {
        Optional<Timesheet> opt = super.findById(id);
        if (opt.isPresent()) {
            Timesheet ts = opt.get();
            try {
                ReflectionSupport.setField(ts, "entries", loadEntries(ts.getId()));
            } catch (SQLException e) {
                throw new JdbcRepositoryException("Failed to load timesheet entries", e);
            }
        }
        return opt;
    }

    @Override
    public List<Timesheet> findAll() {
        List<Timesheet> list = super.findAll();
        try {
            for (Timesheet ts : list) {
                ReflectionSupport.setField(ts, "entries", loadEntries(ts.getId()));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load timesheet entries", e);
        }
        return list;
    }

    @Override
    public List<Timesheet> findByProjectId(int projectId) {
        List<Timesheet> timesheets = queryList("SELECT * FROM timesheets WHERE project_id = ? ORDER BY timesheet_id", projectId);
        try {
            for (Timesheet ts : timesheets) {
                ReflectionSupport.setField(ts, "entries", loadEntries(ts.getId()));
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException("Failed to load timesheet entries by project", e);
        }
        return timesheets;
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

    @Override
    protected Timesheet mapResultSet(ResultSet rs) throws SQLException {
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

    @Override
    protected void bindSaveStatement(PreparedStatement ps, Timesheet entity) throws SQLException {
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
