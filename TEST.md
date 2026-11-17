# Single-lane tunnel: test plan

> Follows the airlock worked example. Ids refer to SPEC.md.

## 1. Criteria

The tests are adequate when:

1. **Coverage.** Every journey, alternative/exception flow and state machine transition is exercised (model coverage, counted with the transition ids in section 2). Code coverage (section 3.1) adds to it, never replaces it. TODO
2. **Requirements verification.** Every property is challenged by at least one scenario; logs match the expected logs, time bounds included; expected logs are written from the specification, never from the program's output. TODO
3. **Design conformance.** Each decision function is reviewed against its state machine, sneak paths included. TODO
4. **Concurrency.** Scenarios with races run several times and every run matches an allowed outcome. TODO

**Limitations.** TODO

## 2. Test scenarios

| # | Scenario | What happens | Inputs | Expected log | New transitions |
|---|---|---|---|---|---|
| S1 | Main scenario | TODO | TODO | TODO | TODO |

## 3. Test implementation

### 3.1 Test architecture

Three kinds of tests. One command, `./mvnw verify`, runs them, the static checks and the coverage gate. No mocks, mutation or performance tests.

| Kind | Target | What it verifies | Tool | Serves |
|---|---|---|---|---|
| Unit | every class on its own | decision functions: every transition (T ids) and every ignored event, as a table (state, message → state, actions); actor runtime: bounded inbox, timers and their cancelling, stop, logging | JUnit 5 | criteria 1, 3 |
| System | the whole system, started by `launcher` on real threads with shortened times (section 3.4) | each scenario of section 2: actual log against expected log; properties over the whole log; races run several times | Cucumber, tracecheck | criteria 1, 2, 4 |
| Architecture | the compiled main classes | the dependencies of SPEC.md Table 5; the Forbidden constructs of AGENTS.md visible in the classes | ArchUnit | D3, D4, D5 |

#### Unit tests

Coverage gate: JaCoCo, with one report for unit and system tests; the build fails below 90% line or 90% branch coverage over the whole project. When coverage falls, add a test; never lower the thresholds or exclude code.

#### System tests

One feature file per scenario in `scenarios/`, next to its expected log, tagged with its ids (`@S1 @P1`, in place of `// Verifies:`). Gherkin gives the inputs: `Given` starts the system, `When` and `And` are user inputs and the passing of time, in real-system time. `Then` steps check properties over the whole log, since a `...` can hide a forbidden message; the last `Then` has tracecheck compare the logs, so no step checks order or times itself. A race is a `Scenario Outline` over run numbers; every run must match an allowed outcome.

One suite runs every feature file, one after another, never in parallel, so that they do not delay each other's threads. System tests use the system only through `launcher` and the messages; no main class uses a test class. tracecheck needs `python3` (3.8 or later).

#### Architecture tests

One rule per row of SPEC.md Table 5, on the project's own packages (the JDK is limited only by the rules below), with `application` matched without its subpackages. Also: `domain` uses only the JDK's language, collection and time types: no threads, queues, clock, logging or I/O (SPEC.md Table 5); no threads, locks, `synchronized` methods or `wait`/`notify` outside `application.actor`; no executors, virtual threads or `CompletableFuture`; no unbounded queue is created: an actor's inbox is its only queue, and timers use it; no catch of `Exception`. `synchronized` blocks, ignored exceptions and catches of `Throwable` are left to the static checks below; a `default` branch on a sealed type, which none of the chosen tools checks out of the box, is left to review.

#### Static checks

Not a kind of test: they read the code, they do not run it. `./mvnw verify` runs them, and any finding fails the build.

| Concern | Tool |
|---|---|
| Compiler warnings | javac, `-Xlint:all -Werror` (as in the airlock example) |
| Formatting | Spotless, with google-java-format |
| Linting | Checkstyle, PMD |
| Bug patterns | SpotBugs |

Guidelines:

- Each tool runs its standard rules. A rule is turned off only where it contradicts SPEC.md or AGENTS.md, in the tool's configuration (never in the code), with its reason, and approved by the students.
- Tools may overlap: a defect reported twice is fixed once. A linter never checks layout, which belongs to the formatter.
- The Forbidden constructs of AGENTS.md that ArchUnit cannot see are checked here where a tool can.
- When a check fails, fix the code; never weaken a check to make the build pass.

### 3.2 The fake environment

TODO

### 3.3 Feature files and expected logs

TODO: the Gherkin step phrases of the feature files, and the expected log syntax (see [tracecheck/README.md](tracecheck/README.md)).

### 3.4 Testing on real threads

TODO: speed factor, `SLACK`, margins between events.
