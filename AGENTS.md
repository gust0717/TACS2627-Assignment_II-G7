# Project rules for AI agents

Read `SPEC.md` first, then `TEST.md`. They are the reference: if the code and the specification disagree, stop and say so, and do not change a property to make a test pass.

This is the controller of a single-lane tunnel with traffic lights at both ends (pool problem 7), in Java 21. Its design is in `SPEC.md`, section 3.

## Architecture

An actor model, hand-built on the JDK, inside a hexagonal boundary: the actors reach the world only through messages. The fake hardware is an adapter started with the system; the test driver plays the users from outside. Packages and their dependencies: `SPEC.md`, section 3.4.

- One component per active object: own thread, single bounded blocking inbox; the shared resource is owned by one coordinator component.
- The inbox, the thread loop, the timers and the run context are written once, in a small kernel that knows nothing of tunnels.
- Each component has a decision function that only decides (no I/O, threads or clock); its thread does what was decided.
- One function starts the system (queues, threads, times, fake hardware settings); components get their queues there.
- The controller uses only the real clock; the test driver gives shorter times to run faster.
- Every message is logged with its time, in the format of the expected logs.
- `scenarios/`: scenario files and `*.expected.txt`; one test runs them all and calls tracecheck; do not reimplement the comparison.

## Conventions

- One small task at a time. Run every test and the linter before reporting a task as finished; report failures as they are.
- Every timing value is a named constant that matches SPEC.md or TEST.md.
- Test first: start from a failing scenario; implement just enough; refactor with all scenarios passing. Never edit an expected log to make a test pass.
- Expected logs come from SPEC.md and TEST.md, never from the program's output: written by the students, or drafted by a session that has not seen the code and reviewed by them. Propose changes; do not edit them unless asked.
- Every failure found by a test becomes a regression test.
- Traceability: `// Implements: P1` in code, `// Verifies: P1` in tests. Say which ids each change serves.
- Tests on threads keep wide margins; where events happen together, the expected log lists every order allowed.
- Design changes go into SPEC.md and TEST.md, not into new documents.

## Forbidden

- shared state between components other than the clock, the log and the stop flag
- unbounded queues
- sleeping or reading the clock in a decision function
- any library beyond the standard one and the test framework, without approval
- thread pools, executors, virtual threads or async tasks (one platform thread per actor)
- a `default` branch on a sealed message type
- catching `Exception` or `Throwable`, or catching any exception and ignoring it (`InterruptedException` only while shutting down, restoring the interrupt flag)
- locks or `synchronized` outside the kernel
