# Agent log

One entry per significant task. Each student records at least two agent changes they rejected or corrected.

## Template

- **Date / author:**
- **Tool and model:**
- **Goal:**
- **Context given:**
- **Prompt (summary):**
- **Accepted:**
- **Wrong, and how it was noticed:**
- **Skill used (if any):**

## Entries

### 1. Repository skeleton
- **Tool and model:** Claude Code, claude-sonnet-5-5
- **Goal:** create the initial structure (SPEC, TEST, AGENTS, README, logs), following the airlock worked example.
- **Context given:** the assignment PDF, the kickoff slides, the airlock repository.
- **Copied from airlock:** `tracecheck/` (unchanged); document structure of SPEC.md, TEST.md and AGENTS.md.

### 2. AGENTS.md for the tunnel (Java 21)
- **Date / author:** 2026-11-14, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** fill the TODOs of AGENTS.md for pool problem 7 in Java 21, keeping the template's structure.
- **Context given:** AGENTS.md, SPEC.md and TEST.md templates, the assignment PDF (sections 4, 6, 7), the airlock AGENTS.md, the aihero.dev AGENTS.md guides.
- **Prompt (summary):** adapt the architecture, conventions and Forbidden list to an actor model inside a hexagonal boundary, in Java 21.
- **Accepted:** actor model in a hexagonal boundary; a small kernel for inbox, loop, timers and run context; expected logs as independent oracles; tracecheck as the only comparison; the Java Forbidden constructs (executors and virtual threads, `default` on sealed message types, catching `Exception`/`Throwable` or ignoring exceptions, locks outside the kernel).
- **Wrong, and how it was noticed:** an independent review (Claude Opus 5.5) found the fake hardware placed outside the system (airlock starts it with the system), the expected-log rule split from assignment section 7, and no exception for `InterruptedException` at shutdown; corrected. Rejected: naming a build command before `pom.xml` exists, and adding a `CLAUDE.md` (Claude Code already reads AGENTS.md).
- **Skill used (if any):** none.

### 3. Foundational architecture (SPEC.md 3.4)
- **Date / author:** 2026-11-14, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** fill SPEC.md section 3.4 and Table 6 with the foundational architecture only, leaving the tunnel components to sections 3.1 to 3.3.
- **Context given:** AGENTS.md (pull request #2), SPEC.md, the assignment PDF, the airlock SPEC.md.
- **Prompt (summary):** specify an actor model inside a ports-and-adapters architecture: packages, their dependencies and the design decisions.
- **Accepted:** packages `launcher`, `adapters`, `application` (with `application.actor` and `application.port`) and `domain` under `pt.feup.tacs.tunnel`; ports as one-way bounded channels; one actor runtime in `application.actor`; decisions D1 to D8; AGENTS.md renamed "kernel" to `application.actor` and "fake hardware" to simulated device adapters. ArchUnit (test library, D5) is pending the teachers' approval.
- **Wrong, and how it was noticed:** an independent review (Claude Opus 5.5) against the assignment, the airlock SPEC.md and AGENTS.md raised findings that were applied and then refined in review; AGENTS.md first listed ArchUnit as allowed before it was approved, corrected to pending.
- **Skill used (if any):** none.

### 4. Test architecture (TEST.md 3.1)
- **Date / author:** 2026-11-15, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** fill TEST.md section 3.1: the kinds of tests, the coverage gate and the architecture tests.
- **Context given:** SPEC.md section 3.4 (pull request #3), TEST.md, AGENTS.md, tracecheck/README.md, the assignment PDF.
- **Prompt (summary):** a brainstorming session on the kinds of tests to support, JaCoCo coverage and ArchUnit dependency tests.
- **Accepted:** three kinds of tests (unit, system, architecture); system scenarios as Gherkin feature files for the inputs, with tracecheck checking the expected logs; ArchUnit rules from SPEC.md Table 5 and the Forbidden list; a JaCoCo gate; `mvn verify` as the one command. Cucumber (cucumber-java, cucumber-junit-platform-engine), ArchUnit and JaCoCo (build plugin) are pending the teachers' approval.
- **Wrong, and how it was noticed:** the agent proposed seven kinds of tests, including component tests of one active object; reduced to three by us. It recommended own scenario files over Cucumber; we chose Cucumber for the inputs, keeping tracecheck for the outputs. It proposed a coverage gate on `domain` only (100% branch); we set 90% line and branch over the whole project. An independent review (Claude Opus 5.5) found that AGENTS.md read as if the libraries were approved, that ArchUnit can check caught exceptions but not unbounded queues in general, and that "tests only through `launcher`" contradicted unit tests; corrected.
- **Skill used (if any):** superpowers:brainstorming, superpowers:writing-plans.

### 5. Static checks (TEST.md 3.1)
- **Date / author:** 2026-11-15, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** choose the formatter, linters and bug-pattern checker, and the guidelines for using them; closes the linter choice left open in entry 4.
- **Context given:** TEST.md and AGENTS.md as of pull request #4, SPEC.md section 3.4, the chosen tools (Spotless, PMD, Checkstyle, SpotBugs).
- **Prompt (summary):** how to adapt AGENTS.md, SPEC.md and TEST.md to Spotless for formatting, PMD and Checkstyle for linting and SpotBugs for bug patterns.
- **Accepted:** Spotless with google-java-format for formatting, Checkstyle and PMD for linting, SpotBugs for bug patterns; compiler warnings as errors, as in the airlock example; general guidelines only (a rule is turned off only where it contradicts the design, with its reason in the tool's configuration; overlap between tools is accepted; never weaken a check to pass); SPEC.md unchanged. The four plugins are pending the teachers' approval.
- **Wrong, and how it was noticed:** the agent first wrote tool-specific settings into TEST.md and AGENTS.md (rule names per Forbidden construct, SpotBugs effort and threshold, suppression syntax, minimum versions); we kept general guidelines only and left the settings to the tools' configuration in the scaffold PR.
- **Skill used (if any):** none.

### 6. Java scaffold
- **Date / author:** 2026-11-15, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** a Maven build for Java 21 that runs every check with one command, with a hello world that proves each library works.
- **Context given:** AGENTS.md, SPEC.md section 3.4 and TEST.md section 3.1 as of pull request #5.
- **Prompt (summary):** a Maven scaffold with a hello world proving each library (unit, system, architecture, logging, configuration).
- **Accepted:** Maven wrapper (Maven 3.9.9, generated from the official Apache tarball, since Homebrew could not install Maven on this machine) and `./mvnw verify` as the one command; the hello world in `launcher` only, writing `Out(Launcher, Hello)` to the log, other packages empty; unit tests, ArchUnit rules from SPEC.md Table 5 and the Forbidden list, and system scenario S0 through Cucumber and tracecheck; the JaCoCo gate (90% line and branch); Spotless, Checkstyle, PMD and SpotBugs with each rule turned off and its reason in `config/`; the `package-info` classes make every package non-empty for ArchUnit, so its rules stay strict (no empty results allowed). `S0_hello.expected.txt` is a draft for review. Pending the teachers' approval: Cucumber, ArchUnit, JaCoCo, Spotless, Checkstyle, PMD, SpotBugs.
- **Wrong, and how it was noticed:** the agent first proposed placeholder classes in every package; reduced by us to `launcher` only. It proposed a global `archRule.failOnEmptyShould=false`; replaced by `allowEmptyShould` on the rules for still-empty packages only. ArchUnit cannot check `catch (Throwable)`, because javac compiles try-with-resources to a synthetic catch of `Throwable`; that check moved to Checkstyle, ArchUnit keeps `catch (Exception)`. TEST.md 3.1 and the `mvn` commands in AGENTS.md and TEST.md updated to match. Task reviews (Claude Opus 5.5) found that the clock, sleep and executor rules missed `LocalDateTime.now()`, `TimeUnit.sleep`, executor subclasses and `Timer`, that JaCoCo appended stale coverage across builds, and that Checkstyle and ArchUnit both checked synchronized methods; all corrected. An independent review (Claude Opus 5.5) of the pull request found that the empty-result allowance was unneeded under Maven and that a commented empty catch passed Checkstyle; corrected, and SpotBugs now also checks the tests. The agent turned off about 30 PMD and SpotBugs rules so that no two tools reported the same defect, each handover resting on a few probes; in review (entry 9) we dropped them: a rule is turned off only where it contradicts the design, and overlap is accepted. Compiler warnings are errors, as in the airlock example.
- **Skill used (if any):** superpowers:brainstorming, superpowers:writing-plans, superpowers:subagent-driven-development.

### 7. Actor runtime design (SPEC.md 3.1.1, 3.2.1)
- **Date / author:** 2026-11-16, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** design the actor runtime in `application.actor`, a light framework for the tunnel's components, before implementing it.
- **Context given:** SPEC.md, TEST.md and AGENTS.md as of pull request #7, the airlock Java example.
- **Prompt (summary):** a brainstorming session on the actor model: base actor, message contracts, how it meets the ports.
- **Accepted:** a runtime generic over message types, on the JDK only, independent of `application.port` (D9); an open base class `Actor<T>` with `onMessage` as the one extension point (D10); one `spawn` per actor, with mailboxes created on first `ref` of a typed `ActorId` (D11); timer and messages ordered by arrival time (D12); `EventLog` as its own class; the inbox capacity, poll time and speed in an `ActorSystemConfig`, with defaults; one `ActorSystem` per run, owning its hidden `RunContext`; diagrams in 3.1.1 and 3.2.1.
- **Wrong, and how it was noticed:** the agent first had the runtime implement `application.port`; we separated them, since a port may be carried by another transport (MQTT, Kafka). It proposed a fixed decide/act loop; we chose an open base class. It proposed two-step wiring (addresses, then actors); we asked for one factory method. It followed the worked example in handling messages before an expired timer; comparing the options on tunnel cases (a busy road starving the maximum-green timeout, the last car leaving just before a clearing timeout) led to ordering by time. A section 3.5 for the runtime was moved to 3.1 and 3.2. It described the tunnel's decision function, ports and decide/act steps in the runtime's section and diagrams; we kept the runtime generic, leaving those to the tunnel's design. An independent review (Claude Opus 5.5) found that a full mailbox could freeze actors (a send to self, a cycle, a crashed actor), that the log was in test time while tracecheck expects real-system time, that `Out` and sends were not tied, that no one wrote the log file, and that one timer per actor was too few for simulated cars; corrected: `self()` removed, a bound on messages in a cycle, `Crash` logged and the run stopped, a speed factor (later moved to `ActorSystemConfig`), `out` before send, `launcher` writes the log in a `finally`, named timers. A proposed `DecidingActor` helper was declined to keep the runtime generic. A Mermaid label with `::` broke a diagram; diagrams now checked with Mermaid's parser.
- **Skill used (if any):** superpowers:brainstorming.

### 8. Actor runtime implementation
- **Date / author:** 2026-11-16, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** implement the actor runtime of SPEC.md 3.1.1 in `application.actor`, with its unit tests and the ArchUnit rules that keep it on the JDK only.
- **Context given:** SPEC.md, TEST.md and AGENTS.md as designed in entry 7; an implementation plan written from them (not kept).
- **Prompt (summary):** execute the plan task by task, test first.
- **Accepted:** `ActorSystemConfig`, `ActorId`, `ActorRef`, `Mailbox` (with `Envelope`), `Actor`, `Timers`, `RunContext`, `EventLog`, `ActorSystem`; 50 runtime tests on real threads, run five times in a row; `application.actor` and `application.port` limited to the JDK by ArchUnit.
- **Wrong, and how it was noticed:** PMD found field names matching methods, a literal in a condition, too many methods and tests with several assertions; fixed in the code (timers moved to `Timers`), no check weakened. A mailbox test was flaky, measuring scheduling delay instead of the stamp; rewritten to test the property. An independent review (Claude Opus 5.5) found that an actor spawned under a second id left that id's mailbox marked as spawned, and that an interrupt ended an actor with no `Crash` line; both fixed test first. Seven minor findings deferred (null messages, stamp bound, lines after stop, `stop()` from an actor's thread, crash visibility for the launcher, timers set from another thread, refusal messages untested).
- **Skill used (if any):** superpowers:writing-plans, superpowers:executing-plans, superpowers:test-driven-development.

### 9. Review of the stack (pull requests #2 to #8)
- **Date / author:** 2026-11-16, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-fable-5-2
- **Goal:** review the draft pull requests as one stack, for gaps, stale content and simplifications, and fix each finding in its pull request.
- **Context given:** the six pull requests; SPEC.md, TEST.md and AGENTS.md as they are after the last one.
- **Prompt (summary):** look for gaps and misconceptions, stale content, improvements and simplifications; keep the documents short.
- **Accepted:** timers divided by the speed in the runtime, with a test at speed 4 (D8: nothing divided them before); the clock multiplied before truncating to ms; a `Crash` line fails every scenario; a plain `boolean` for a mailbox's spawned flag, which the system's lock already guards; `mailboxCapacity`; `package-info` files reduced to one line pointing to Table 5; stale lines on `launcher` and the JaCoCo version corrected; the static-check guidelines changed from "no two tools check the same thing" to "a rule is turned off only where it contradicts the design", dropping about 30 exclusions, and compiler warnings made errors, as in the airlock example; the implementation plan removed.
- **Wrong, and how it was noticed:** the review replaced the runtime's `ConcurrentHashMap`s by `HashMap`s, as one lock or one thread already guards them; PMD (`UseConcurrentHashMap`) refused it, and they were kept rather than weaken the check. It also proposed logging S0 through the runtime's `EventLog`; not done, since the log's time would no longer be exactly 0 ms and wiring `launcher` to the runtime is the next iteration. Renumbering Figures 7 to 9 was left until sections 3.1 to 3.3 have their diagrams.
- **Skill used (if any):** none.

### 10. Actor runtime without a log (SPEC.md 3.1.1, D8, D13)
- **Date / author:** 2026-11-17, Bruno Alves
- **Tool and model:** Claude Code (desktop), claude-opus-5-5
- **Goal:** review pull request #8 so that the actor runtime holds only what a minimal actor model needs, with nothing of the problem.
- **Context given:** pull request #8; SPEC.md, TEST.md and AGENTS.md as of that pull request.
- **Prompt (summary):** can the log be handled outside the runtime, by the components that address the problem?
- **Accepted:** the runtime knows no log format: `EventLog`, `out`, `ActorSystem.log()` and the `In`, `Out` and `Crash` texts leave it; it reports every delivery (an expired timer's included, on the actor's thread, before `onMessage`) and every crash to one `ActorObserver` per system (D13), so D7's "every message" is still kept in the loop. The speed factor leaves the runtime too: `launcher` gives shortened times and the event log multiplies its times back (D8). The event log itself, its format, `Out` and the speed are left to the tunnel's implementation (SPEC.md 3.1, a TODO). Documents first, then the code, test first: `ActorObserver` (with `NONE`) added, `EventLog`, `out` and the speed removed, `RunContext` reduced to the stop flag and the observer; the runtime tests read a test `Recorder` instead of the log; new tests for each part of the observer's contract (on the actor's thread before `onMessage`, crash reported before the run stops and stopping even if the observer throws, an observer's exception is a crash); the speed-4 timer test removed with the speed, to come back with `launcher`. `./mvnw verify` passes; the runtime tests passed five runs in a row.
- **Wrong, and how it was noticed:** the runtime of entries 7 to 9 held the expected logs' line format, the `In`, `Out` and `Crash` texts and a clock multiplied by the speed only for the log, so it was tied to this project's tests; noticed by us reviewing the pull request. The agent first proposed a log package (`application.log`) in this pull request; we kept only the extension point, as the log belongs to the tunnel's implementation.
- **Skill used (if any):** none.
