import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/*
 * Task 4 - connection leak in ReportDAO
 *
 * What was happening: app runs fine for about 6 hours, then hangs. DBA
 * traced it to the connection pool being fully checked out - we were
 * never returning connections.
 *
 * Rule: don't change the SQL or mapRow(). Just fix the resource handling.
 */
public class Task4 {

    public static class ReportDAO {

        private DataSource dataSource;

        // FIX: the old code opened a Connection, a PreparedStatement and a
        // ResultSet, and then never closed any of them. Every call leaked
        // all three. Switched to try-with-resources so they close on the way
        // out no matter what (exception or not).
        //
        // The nesting matters: try-with-resources closes in reverse order of
        // declaration, and the inner block finishes before the outer one,
        // so we end up closing in the order the spec asks for:
        //     ResultSet -> PreparedStatement -> Connection.
        public List<ReportEntry> fetchMonthlyReport(String accountId, int month, int year)
                throws SQLException {
            String sql = "SELECT * FROM report_entries "
                    + "WHERE account_id = ? AND MONTH(entry_date) = ? "
                    + "AND YEAR(entry_date) = ?";

            List<ReportEntry> entries = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, accountId);
                ps.setInt(2, month);
                ps.setInt(3, year);

                // ResultSet is AutoCloseable too, so put it in its own
                // try-with-resources. This way it gets closed even if
                // mapRow() throws halfway through the loop.
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        entries.add(mapRow(rs));
                    }
                }
            }
            return entries;
        }

        private ReportEntry mapRow(ResultSet rs) throws SQLException {
            // unchanged
            return new ReportEntry();
        }
    }

    static class ReportEntry { }
}
