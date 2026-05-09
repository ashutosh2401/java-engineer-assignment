import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * Task 2 - ConcurrentModificationException analysis
 *
 * Stack trace we got from the bank:
 *   java.util.ConcurrentModificationException
 *     at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:911)
 *     at java.util.ArrayList$Itr.next(ArrayList.java:861)
 *     at c.s.dlp.service.StatementProcessorService
 *         .filterTransactions(StatementProcessorService.java:142)
 *
 * --------------------------------------------------------------------------
 * Q1. What actually causes a ConcurrentModificationException?
 *
 * ArrayList (and most of the regular java.util collections) are "fail-fast".
 * The list keeps a counter inside it called modCount, and bumps it every
 * time the structure changes - so any add, remove, clear, etc.
 *
 * When you ask for an Iterator, the iterator notes down whatever modCount
 * was at that moment as expectedModCount. Then on every next() call it
 * checks: do these two still match? If they don't, the list got changed
 * behind the iterator's back, and it throws CME right there instead of
 * letting you read inconsistent data.
 *
 * Worth noting: the name says "concurrent" but you don't need two threads.
 * One thread iterating and mutating the same list will trip this just as
 * happily. It just shows up *more* under load because more requests are
 * doing it at the same time.
 *
 * --------------------------------------------------------------------------
 * Q2. What did line 142 most likely look like?
 *
 * Classic shape - mutate the list you're iterating with a for-each loop
 * (a for-each is really just a hidden Iterator):
 *
 *     for (Transaction t : transactions) {          // line 142
 *         if (t.isReversed()) {
 *             transactions.remove(t);               // boom -> CME
 *         }
 *     }
 *
 * Other thing to rule out: if `transactions` is a shared field on the
 * service and another thread is add/remove-ing it while this one iterates,
 * you'd get the same exception from the other direction. But the simple
 * iterate-and-remove pattern above is what fits this trace.
 *
 * --------------------------------------------------------------------------
 * Q3. Smallest safe fix?
 *
 * Use the iterator's own remove() so modCount and expectedModCount stay
 * lined up:
 *
 *     Iterator<Transaction> it = transactions.iterator();
 *     while (it.hasNext()) {
 *         if (it.next().isReversed()) it.remove();
 *     }
 *
 * Or, since this is Java 8+, just one line:
 *
 *     transactions.removeIf(Transaction::isReversed);
 *
 * If profiling later shows the list really is being shared across threads,
 * swap the field type for CopyOnWriteArrayList - but for the
 * single-threaded iterate-and-mutate pattern that matches this stack
 * trace, removeIf is the minimal fix.
 */
public class Task2Analysis {

    // What the original code probably looked like - kept here so you can
    // see the bug for yourself. Running this throws CME.
    static List<String> brokenFilter(List<String> txns) {
        for (String t : txns) {
            if (t.startsWith("REV-")) {
                txns.remove(t); // throws ConcurrentModificationException
            }
        }
        return txns;
    }

    // Fix option 1: use the iterator's own remove() method.
    static List<String> fixedFilterIterator(List<String> txns) {
        Iterator<String> it = txns.iterator();
        while (it.hasNext()) {
            if (it.next().startsWith("REV-")) {
                it.remove();
            }
        }
        return txns;
    }

    // Fix option 2: same thing, one line, Java 8+.
    static List<String> fixedFilterRemoveIf(List<String> txns) {
        txns.removeIf(t -> t.startsWith("REV-"));
        return txns;
    }

    public static void main(String[] args) {
        List<String> txns = new ArrayList<>();
        txns.add("OK-1");
        txns.add("REV-2");
        txns.add("OK-3");
        System.out.println(fixedFilterRemoveIf(txns));
    }
}
