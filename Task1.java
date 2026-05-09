import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/*
 * Task 1 - getOverdueLoans()
 *
 * Two things were going wrong in prod:
 *   1. NPE for some accounts.
 *   2. Wrong output when an account had a zero balance.
 *
 * About LoanAccount:
 *   dueDate            -> can be null (restructured accounts)
 *   outstandingBalance -> double
 *   accountId          -> always set
 */
public class Task1 {

    public List<LoanAccount> getOverdueLoans(List<LoanAccount> accounts) {
        // FIX 1: this used to be `null`, which blew up on result.add()
        // and also meant we returned null when nothing matched. Callers expect a list back, so start with an empty one.
        List<LoanAccount> result = new ArrayList<>();

        // FIX 2: if someone hands us a null list, just return empty
        // instead of throwing NPE on the for-each.
        if (Objects.isNull(accounts)) {
            return result;
        }

        Date now = new Date();
        for (LoanAccount account : accounts) {
            // skip stray nulls in the list rather than crash
            if (Objects.isNull(account)) {
                continue;
            }
            // FIX 3: dueDate can be null for restructured accounts, and
            // calling .before() on null is what was throwing the NPE.
            // No due date -> not overdue, so just skip.
            Date dueDate = account.getDueDate();
            if (Objects.nonNull(dueDate)
                    && dueDate.before(now)
                    && account.getOutstandingBalance() > 0) {
                result.add(account);
            }
        }
        return result;
    }

    static class LoanAccount {
        private Date dueDate;
        private double outstandingBalance;
        private String accountId;

        public Date getDueDate() { return dueDate; }
        public double getOutstandingBalance() { return outstandingBalance; }
        public String getAccountId() { return accountId; }
    }
}
