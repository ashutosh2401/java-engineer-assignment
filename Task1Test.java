import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * Bonus JUnit tests for Task 1 - reproduces each of the three original
 * defects in getOverdueLoans() and asserts the fix.
 *
 * Run with:
 *   javac -cp lib/junit-platform-console-standalone-1.10.2.jar Task1.java Task1Test.java
 *   java  -jar lib/junit-platform-console-standalone-1.10.2.jar \
 *         --class-path . --select-class Task1Test
 */
class Task1Test {

    private final Task1 svc = new Task1();

    // ---------- defect 1: result was initialized to null ----------

    @Test
    @DisplayName("defect 1: returns empty list (not null) when no accounts match")
    void returnsEmptyListWhenNothingOverdue() {
        Task1.LoanAccount a = account("A1", futureDate(), 100.0);
        List<Task1.LoanAccount> result = svc.getOverdueLoans(Collections.singletonList(a));
        assertNotNull(result, "old code returned null - new code must return a list");
        assertTrue(result.isEmpty(), "future-dated account is not overdue");
    }

    @Test
    @DisplayName("defect 1: adding an overdue account no longer NPEs")
    void doesNotNpeOnOverdueAccount() {
        Task1.LoanAccount a = account("A1", pastDate(), 500.0);
        // before the fix, result was null and result.add(a) threw NPE here
        List<Task1.LoanAccount> result = svc.getOverdueLoans(Collections.singletonList(a));
        assertEquals(1, result.size());
        assertEquals("A1", result.get(0).getAccountId());
    }

    // ---------- defect 2: null accounts argument ----------

    @Test
    @DisplayName("defect 2: null accounts list returns empty list, not NPE")
    void handlesNullAccountsList() {
        List<Task1.LoanAccount> result = svc.getOverdueLoans(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------- defect 3: null dueDate (restructured account) ----------

    @Test
    @DisplayName("defect 3: account with null dueDate is skipped, not NPE")
    void handlesNullDueDate() {
        Task1.LoanAccount restructured = account("R1", null, 999.0);
        Task1.LoanAccount overdue      = account("O1", pastDate(), 200.0);

        List<Task1.LoanAccount> result =
                svc.getOverdueLoans(Arrays.asList(restructured, overdue));

        assertEquals(1, result.size(), "only the overdue one should be returned");
        assertEquals("O1", result.get(0).getAccountId());
    }

    // ---------- zero-balance behaviour (the second symptom in the bug report) ----------

    @Test
    @DisplayName("zero-balance overdue account is excluded")
    void zeroBalanceIsNotOverdue() {
        Task1.LoanAccount paidOff = account("P1", pastDate(), 0.0);
        List<Task1.LoanAccount> result = svc.getOverdueLoans(Collections.singletonList(paidOff));
        assertTrue(result.isEmpty(), "an account with nothing owing is not overdue");
    }

    @Test
    @DisplayName("happy path: filters mixed list correctly")
    void filtersMixedList() {
        List<Task1.LoanAccount> input = Arrays.asList(
                account("OVERDUE",        pastDate(),   100.0),
                account("FUTURE",         futureDate(), 100.0),
                account("ZERO_BAL_LATE",  pastDate(),     0.0),
                account("RESTRUCTURED",   null,         500.0)
        );

        List<Task1.LoanAccount> result = svc.getOverdueLoans(input);

        assertEquals(1, result.size());
        assertEquals("OVERDUE", result.get(0).getAccountId());
    }

    // ---------- helpers ----------

    private static Date pastDate() {
        return new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000); // yesterday
    }

    private static Date futureDate() {
        return new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000); // tomorrow
    }

    /**
     * The LoanAccount stub in Task1 has no constructor on purpose (it's a
     * minimal placeholder for compilation), so we set its private fields
     * via reflection. This keeps Task1.java untouched.
     */
    private static Task1.LoanAccount account(String id, Date dueDate, double balance) {
        try {
            Task1.LoanAccount a = new Task1.LoanAccount();
            setField(a, "accountId", id);
            setField(a, "dueDate", dueDate);
            setField(a, "outstandingBalance", balance);
            return a;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
