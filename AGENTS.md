# Project rules for AI agents

Read `SPEC.md` first, then `TEST.md`. They are the reference: if the code and the specification disagree, stop and say so, and do not change a property to make a test pass.

## Architecture

TODO: adapt from the reference architecture:

- One component per active object: own thread, single bounded blocking inbox; the shared resource is owned by one coordinator component.
- Each component has a decision function that only decides (no I/O, threads or clock); its thread does what was decided.
- One function starts the system (queues, threads, times, fake hardware settings); components get their queues there.
- The controller uses only the real clock; the test driver gives shorter times to run faster.
- Hardware is faked by threads; the test driver plays the users.
- Every message is logged with its time, in the format of the expected logs.
- `scenarios/`: scenario files and `*.expected.txt`; one test runs them all.
- Expected logs are written by the students; propose changes, do not edit them unless asked.

## Conventions

- One small task at a time. Run every test and the linter before reporting a task as finished; report failures as they are.
- Every timing value is a named constant that matches SPEC.md or TEST.md.
- Test first: start from a failing scenario; implement just enough; refactor with all scenarios passing. Never edit an expected log to make a test pass.
- Every failure found by a test becomes a regression test.
- Traceability: `// Implements: P1` in code, `// Verifies: P1` in tests. Say which ids each change serves.
- Tests on threads keep wide margins; where events happen together, the expected log lists every order allowed.

## Forbidden

- shared state between components other than the clock, the log and the stop flag
- unbounded queues
- sleeping or reading the clock in a decision function
- any library beyond the standard one and the test framework, without approval
- TODO: risky constructs of the chosen language (e.g. `unwrap()` in Rust, bare `except:` in Python)
