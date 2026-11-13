# TACS 2026/27, Assignment II (Group 7): Agentic MDE for Embedded Controllers

Course: Advanced Software Construction Techniques (TACS), FEUP, 2026/27.

Controller: **TODO** (from the project pool)
Language: **TODO**

An embedded controller built from behavioural models (state machines, sequence diagrams, temporal properties), turned into concurrent code by AI agents and checked by timed scenario tests. The architecture follows the teachers' [airlock worked example](https://github.com/joaopascoalfariafeup/airlock).

## Repository layout

| Path | Content |
|---|---|
| [SPEC.md](SPEC.md) | Requirements and design |
| [TEST.md](TEST.md) | Test plan: criteria, scenarios, how tests are built |
| [AGENTS.md](AGENTS.md) | Rules and context for AI agents |
| [AGENT_LOG.md](AGENT_LOG.md) | What each agent did, what was accepted or rejected |
| [RETROSPECTIVE.md](RETROSPECTIVE.md) | Final reflection |
| `scenarios/` | Scenario files and expected logs |
| [tracecheck/](tracecheck/README.md) | Log comparison tool (from the airlock example) |
| `.github/skills/` | Agent skills |
| `src/` (TODO) | Controller code and tests, per the chosen language |
| [Referencias/](Referencias) | Assignment text and kickoff slides |

## Run it

TODO: the one command that runs every check (e.g. `cargo test`, `mvn test`, `pytest`).

## Process

Specify (SPEC, TEST) → Generate (one scenario at a time, test first) → Verify (logs vs expected logs, state machine review) → Refine. Changes to SPEC, TEST and AGENTS go through reviewed pull requests.

## Team

TODO: members and roles.

## License

MIT, see [LICENSE](LICENSE). `tracecheck/` is copied from the airlock example.
