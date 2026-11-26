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
