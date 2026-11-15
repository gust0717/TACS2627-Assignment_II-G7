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
