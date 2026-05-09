import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Task 5 - exception handling in DocumentValidator
 *
 * What support is seeing:
 *   - Logs full of stack traces for stuff that's just normal validation
 *     failure (null doc, empty content). Real errors get buried.
 *   - The batch loop's catch block is empty, so when something actually
 *     breaks nobody finds out.
 *
 * Rule: don't touch runValidationRules() or saveResult().
 *
 * Four issues, each tagged with // FIX: below.
 */
public class Task5 {

    private static final Logger log = LoggerFactory.getLogger(Task5.class);

    public ValidationResult validate(Document doc) {
        // FIX 1: the old code threw plain RuntimeException for things like
        // "doc is null" and then did e.printStackTrace() in one big catch.
        // That dumped a stack trace for every routine validation failure -
        // hence the log flood. Fix: throw a specific ValidationException
        // for the expected stuff and catch it separately (warn, no stack).
        // Anything else falls through to the generic catch below as a real
        // error (error level, with the stack).
        try {
            if (doc == null) {
                throw new ValidationException("Document is null");
            }
            String content = doc.extractContent();
            if (content == null || content.isEmpty()) {
                throw new ValidationException("Empty content");
            }
            return runValidationRules(content);
        } catch (ValidationException ve) {
            // expected business failure - just a warn line, no stack trace
            log.warn("Validation failed: {}", ve.getMessage());
            // FIX 2: used to return null here. That made every caller
            // null-check, and validateBatch was actually NPE-ing on
            // r.isValid(). Better contract: always return a result object,
            // just mark it invalid.
            return ValidationResult.invalid(ve.getMessage());
        } catch (Exception e) {
            // genuine surprise - this one we DO want to see in full
            log.error("Unexpected error while validating document", e);
            return ValidationResult.invalid("Internal error: " + e.getMessage());
        }
    }

    public void validateBatch(List<Document> docs) {
        for (Document doc : docs) {
            try {
                ValidationResult r = validate(doc);
                // FIX 3: original code did `if (r.isValid())` straight up,
                // which exploded as soon as validate() returned null (see
                // issue 2). Once issue 2 is fixed r should never be null,
                // but a belt-and-braces null check costs nothing and stops
                // a future regression from killing the whole batch.
                if (r != null && r.isValid()) {
                    saveResult(r);
                }
            } catch (Exception e) {
                // FIX 4: this catch used to be empty - the batch quietly
                // ate every exception, including real bugs in saveResult().
                // Log it at error level with the doc id so a) the batch
                // keeps running for the rest of the docs, and b) ops have
                // something to grep when a record goes missing.
                log.error("Failed to process document in batch: {}",
                        doc != null ? doc.getId() : "<null>", e);
            }
        }
    }

    // ---- unchanged 

    private ValidationResult runValidationRules(String content) {
        return ValidationResult.valid();
    }

    private void saveResult(ValidationResult r) {
        // unchanged
    }

    // ---- supporting types

    static class Document {
        String id;
        String getId() { return id; }
        String extractContent() { return ""; }
    }

    static class ValidationResult {
        private final boolean valid;
        private final String reason;
        private ValidationResult(boolean valid, String reason) {
            this.valid = valid; this.reason = reason;
        }
        static ValidationResult valid() { return new ValidationResult(true, null); }
        static ValidationResult invalid(String reason) { return new ValidationResult(false, reason); }
        boolean isValid() { return valid; }
        String getReason() { return reason; }
    }

    static class ValidationException extends RuntimeException {
        ValidationException(String msg) { super(msg); }
    }
}
