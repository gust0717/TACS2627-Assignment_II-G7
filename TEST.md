# TODO: <controller name>: test plan

> Template following the airlock worked example. Ids refer to SPEC.md.

## 1. Criteria

The tests are adequate when:

1. **Coverage.** Every journey, alternative/exception flow and state machine transition is exercised. TODO
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

TODO: test driver, run context, scenarios folder, tracecheck; the one command that runs everything.

### 3.2 The fake environment

TODO

### 3.3 Scenario files and expected logs

TODO: scenario file syntax and expected log syntax (see [tracecheck/README.md](tracecheck/README.md)).

### 3.4 Testing on real threads

TODO: speed factor, `SLACK`, margins between events.
