# Java Support Assignment - Bug Resolution

Submission for the Java Developer (Support) take-home. Five tasks fixed with
inline `// FIX:` notes; bonus JUnit test for Task 1.

## Layout

```
.
├── Task1.java              Task 1 - LoanAccountService.getOverdueLoans()
├── Task2Analysis.java      Task 2 - ConcurrentModificationException write-up
├── Task3.java              Task 3 - thread-safety in BankStatementBatchProcessor
├── Task4.java              Task 4 - connection leak in ReportDAO
├── Task5.java              Task 5 - exception handling in DocumentValidator
├── Task1Test.java          Bonus: JUnit 5 tests for Task 1
├── build.sh                One-shot build script
└── lib/
    ├── slf4j-api-2.0.13.jar                          (used by Task 5)
    └── junit-platform-console-standalone-1.10.2.jar  (runs the bonus test)
```

## Requirements

- Java 8 or above (`java -version`)
- Bash (for `build.sh`)

No Maven / Gradle / network access needed - the two jars under `lib/` are
the only dependencies and they ship with the submission.

## Build

From the project root:

```bash
./build.sh
```

This compiles everything into `out/`. Tasks 1-4 are pure JDK; Task 5 is
compiled with `slf4j-api` on the classpath; `Task1Test` is compiled with
the JUnit jar on the classpath.

If you prefer to compile manually:

```bash
mkdir -p out

# Tasks 1-4 (plain JDK)
javac -d out Task1.java Task2Analysis.java Task3.java Task4.java

# Task 5 (needs SLF4J)
javac -cp lib/slf4j-api-2.0.13.jar -d out Task5.java

# Bonus test (needs JUnit 5)
javac -cp lib/junit-platform-console-standalone-1.10.2.jar:out \
      -d out Task1Test.java
```

## Run

### Run the bonus JUnit tests (Task 1)

After `./build.sh`:

```bash
java -jar lib/junit-platform-console-standalone-1.10.2.jar \
     execute --class-path out --select-class Task1Test
```

Expected output: **6 tests successful, 0 failed.**

### Run the Task 2 demo

`Task2Analysis` has a tiny `main()` that runs the fixed filter so you can
see it in action:

```bash
java -cp out Task2Analysis
# -> [OK-1, OK-3]
```

Tasks 1, 3, 4 and 5 are libraries (no `main`) - they're meant to be read
and reviewed, not executed standalone.

## Where the answers live

- **Task 2's three written answers** are at the top of
  [Task2Analysis.java](Task2Analysis.java) as a block comment.
- Every fix is annotated inline with a `// FIX:` comment naming the defect
  and explaining the change.

## Notes on scope

Per the assignment rules, no new classes, endpoints or business logic
were introduced; method signatures were preserved; existing comments were
kept. The only `// FIX:` annotations are on lines that actually changed.

The small inner stub classes (`LoanAccount`, `StatementRecord`,
`ReportEntry`, `Document`, `ValidationResult`, `ValidationException`)
exist only so each task file compiles standalone - they would not be
included when dropping the fixed methods back into the real codebase.
