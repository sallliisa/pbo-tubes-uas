# Phase 0 Baseline Audit (UTS)

Date: 2026-05-21
Branch: tubes-uas-refactor

## Compile and Run Baseline

- Compile command: `javac -d bin src/*.java src/exceptions/*.java src/*/*.java`
- Run command: `java -cp bin App`
- Result: compile succeeded, application ran successfully to completion (`=== End of Program ===`).

## Baseline Runtime Behavior (Observed)

- Employee salary update outside position range fails and is handled with fallback salary.
- Timesheet approval before submit fails and is handled by submit-then-approve flow.
- Rejected and approved timesheet flows both execute.
- Contract lifecycle includes signed, renewed, and terminated states.
- Invoice lifecycle includes generated, signed, sent, and paid states.
- Program finishes without uncaught exception.

## Domain Classes Identified

- `Employee`
- `PermanentEmployee`
- `ContractEmployee`
- `Department`
- `Position`
- `Client`
- `Project`
- `ProjectAssignment`
- `Timesheet`
- `TimesheetEntry`
- `Contract`
- `Invoice`

## Workflow/Status Classes Identified

- `ProjectStatus`
- `TimesheetStatus`
- `ContractStatus`
- `InvoiceStatus`

## Existing OO Concepts Identified

- Inheritance: `Employee`, `PermanentEmployee`, `ContractEmployee`
- Interface: `Signable`
- Overloading:
  - `Project.assignEmployee(...)`
  - `Timesheet.addEntry(...)`
  - `Contract.renew(...)`
- Collections:
  - lists of assignments, timesheets, invoices, contracts
- Functional style:
  - stream-based calculations/filtering (`stream()` usage in `Client`, `Department`, `Project`, `Timesheet`)

## Scope Guard

- No business logic changed in this phase.
- No package restructuring performed in this phase.
