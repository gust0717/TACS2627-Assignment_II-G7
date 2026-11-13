# TODO: <controller name>: specification

> Template following the airlock worked example. Every item has an id (J, A, E, P, T, D, S) cited in the docs, the code (`// Implements: P1`) and the tests (`// Verifies: P1`).

## 1. Context

TODO: what the controller does; its sensors, actuators and users.

## 2. Requirements

### 2.1 Functional requirements

#### Table 1. Users' journeys
| # | Journey |
|---|---|
| J1 | TODO |

#### Table 2. Alternative and exception flows
| # | Flow | What the controller does |
|---|---|---|
| A1 | TODO | TODO |
| E1 | TODO | TODO |

### 2.2 Non-functional requirements: temporal properties

#### Table 3. Temporal properties
| # | Kind | Property | Hazard if violated |
|---|---|---|---|
| P1 | safety | TODO | TODO |
| P2 | bounded response | TODO (state the assumptions and faults under which it holds) | TODO |

### 2.3 Main journey, as the user sees it

TODO: Mermaid sequence diagram (user and system).

### 2.4 Constraints, assumptions and exclusions

Constraints: TODO

Assumptions: TODO

Exclusions: TODO

## 3. Design

### 3.1 Structure

TODO: structural diagram (communication, component or class), message table (Table 4), and how the shared resource is requested, granted and released.

### 3.2 Main scenario with internal interactions (S1)

TODO: Mermaid sequence diagram with the internal participants.

### 3.3 State machines

TODO: one Mermaid state machine per concurrent component, transitions numbered (T1, T2, ...). Say what is ignored and what is latched.

### 3.4 Implementation modules and dependencies

TODO: dependency diagram and module table (Table 5).

### 3.5 Summary of design decisions

#### Table 6. Design decisions
| # | Decision | Rationale | Serves |
|---|---|---|---|
| D1 | TODO | TODO | TODO |
