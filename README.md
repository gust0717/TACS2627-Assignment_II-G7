# TACS 2026/27, Assignment II (Group 7): Agentic MDE for Embedded Controllers

Course: Advanced Software Construction Techniques (TACS), FEUP, 2026/27.

Controller: **single-lane tunnel with traffic lights at both ends** (pool problem 7)
Language: **Java 21**

An embedded controller built from behavioural models (state machines, sequence diagrams, temporal properties), turned into concurrent code by AI agents and checked by timed scenario tests. The architecture follows the teachers' [airlock worked example](https://github.com/joaopascoalfariafeup/airlock).

## Repository layout

| Path | Content |
|---|---|
| [SPEC.md](SPEC.md) | Requirements and design |
| [TEST.md](TEST.md) | Test plan: criteria, scenarios, how tests are built |
| [AGENTS.md](AGENTS.md) | Rules and context for AI agents |
| [AGENT_LOG.md](AGENT_LOG.md) | What each agent did, what was accepted or rejected |
| [RETROSPECTIVE.md](RETROSPECTIVE.md) | Final reflection |
| `scenarios/` | Feature files (Gherkin) and expected logs |
| [tracecheck/](tracecheck/README.md) | Log comparison tool (from the airlock example) |
| `.github/skills/` | Agent skills |
| `.github/workflows/` | The CI pipeline |
| `src/` | `main/java`: controller code under `pt.feup.tacs.tunnel` (`launcher`, `adapters`, `application`, `domain`); `test/java`: unit tests per package, `architecture` (ArchUnit) and `system` (Cucumber runner for `scenarios/`) |
| `config/` | Checkstyle and PMD settings, with the reason for each rule turned off |
| [Referencias/](Referencias) | Assignment text and kickoff slides |

## Run it

```
./mvnw verify
```

Needs JDK 21 or later and python3 3.8 or later (for tracecheck). The Maven wrapper downloads Maven 3.9.9. The command runs, in order:

- compile with Java 21, every warning an error;
- unit and architecture tests (JUnit 5, ArchUnit) in Surefire;
- system tests in Failsafe: each `scenarios/*.feature` runs through Cucumber, and tracecheck compares the log with its `*.expected.txt`;
- Spotless: the code is formatted with google-java-format (`./mvnw spotless:apply` fixes it);
- Checkstyle and PMD: lint rules from `config/`;
- SpotBugs: bug patterns;
- JaCoCo: at least 90% line and branch coverage over unit and system tests.

## Continuous integration

CI runs the same gate as `./mvnw verify`, in three stages that stop at the first failure:

1. compile and static checks (Spotless, Checkstyle, PMD, SpotBugs);
2. unit and architecture tests;
3. system tests and the coverage gate.

It runs on every pull request ready for review, into any branch, and on every push to `main`. Draft pull requests are skipped. It can also be started by hand on any branch ("Run workflow" in the Actions tab); a manual run does not publish a release. Every passing merge to `main` publishes a release with the jar and the coverage report. Merging into `main` is meant to require the `build` check to pass and the branch to be up to date with `main`. The rule is a ruleset on `main` in the repository settings, not code, set by the repository owner.

## Process

Specify (SPEC, TEST) → Generate (one scenario at a time, test first) → Verify (logs vs expected logs, state machine review) → Refine. Changes to SPEC, TEST and AGENTS go through reviewed pull requests.

## Team

TODO: members and roles.

## License

MIT, see [LICENSE](LICENSE). `tracecheck/` is copied from the airlock example.
