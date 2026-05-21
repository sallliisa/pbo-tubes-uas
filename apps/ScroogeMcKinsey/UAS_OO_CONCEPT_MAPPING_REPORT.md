# UAS OO Concept Mapping Report

## 1. System Overview

This codebase models a consulting-company information system with workflows for:
- organization setup (department, position, employee),
- project staffing and assignment,
- timesheet lifecycle,
- contract and invoice lifecycle,
- reporting, and
- approval queue orchestration.

The final demonstration is executed in `src/app/App.java`.

## 2. Refactoring Motivation

The original implementation was functional but concentrated orchestration in one place.  
The refactor improves maintainability by separating:
- domain rules (`domain/*`),
- workflow orchestration (`service/*`),
- storage abstraction (`repository/*`),
- reusable workflow collection (`workflow/*`),
- demo runner (`app/App.java`).

This keeps business behavior explicit while preparing the codebase for future persistence and backend modularization.

## 3. Modularization Design

Package roles:
- `app`: demo runner and scenario orchestration entry point.
- `domain`: business entities and enums, including constraints and lifecycle rules.
- `service`: cross-entity workflow coordination.
- `repository`: generic repository abstraction and in-memory implementation.
- `workflow`: reusable queue abstraction for approval pipelines.
- `exception`, `exceptions`, `validation`: error and validation support.

## 4. Polymorphism Implementation

### Ad-hoc polymorphism (overloading)

Implemented with domain-relevant overloaded methods:
- `domain.project.Project.assignEmployee(...)` (default vs explicit date range)
- `domain.timesheet.Timesheet.addEntry(...)` (entry object vs raw fields)
- `domain.billing.Contract.renew(...)` (with/without new value)

These overloads reduce boilerplate while preserving natural business operations.

### Ad-hoc polymorphism (coercion)

Not explicitly implemented as a dedicated value-object coercion API (e.g. `WorkHours.of(...)`).  
This was intentionally skipped to avoid adding extra objects that are not yet required by current workflows.

### Universal polymorphism (inclusion)

Employee hierarchy:
- base: `domain.organization.Employee`
- subtypes: `PermanentEmployee`, `ContractEmployee`

Runtime demonstration:
- `service.PayrollService` processes mixed employee subtypes through `Employee` abstraction in `App.java`.

### Universal polymorphism (interface-based)

Signable documents:
- interface: `domain.common.Signable`
- implementations: `domain.billing.Contract`, `domain.billing.Invoice`

Runtime demonstration:
- `service.SigningService.sign(...)` and `signAll(...)` handle documents through `Signable` without concrete-type branching.

### Universal polymorphism (parametric)

Repository abstractions:
- `repository.Repository<ID, T extends Identifiable<ID>>`
- `repository.InMemoryRepository<ID, T extends Identifiable<ID>>`

These use parametric types to support reusable storage logic across entities.

## 5. Generics Implementation

### Generic classes
- `repository.InMemoryRepository<ID, T>`
- `workflow.ApprovalQueue<T>`

### Generic methods
- `service.ReportService.filter(Collection<T>, Predicate<T>)`
- `service.ReportService.map(Collection<T>, Function<T, R>)`
- `service.SigningService.signAll(List<? extends Signable>, String)`

### Upper-bounded wildcard
- `service.PayrollService.calculateTotalCompensation(Collection<? extends Employee>)`
- `service.PayrollService.calculateCompensations(Collection<? extends Employee>)`

These methods read from subtype collections safely.

### Lower-bounded wildcard
- `service.EmployeeService.copyEmployees(Collection<? extends Employee>, Collection<? super Employee>)`

This method supports copying subtype employee collections into broader destination collections.

## 6. Collection Usage

Core generic collections used:
- `List<T>` for entity aggregates and report outputs.
- `Map<ID, T>` in `InMemoryRepository` for O(1)-style lookup by identity.
- `Queue<T>` in `ApprovalQueue` for ordered workflow handling.

The collections remain domain-relevant and type-safe (no raw collection types in active workflow code).

## 7. Cross-Paradigm Features

### Lambda functions

Used in `App.java` with generic report methods, e.g.:
- active project filtering,
- approved timesheet filtering,
- project summary mapping.

### Method references

Used in reporting and retrieval flows, e.g.:
- `Employee::getFullName`
- `Timesheet::getTotalHours`

### Higher-order functions

Implemented in `ReportService` via methods accepting:
- `Predicate<T>` (`filter`)
- `Function<T, R>` (`map`)

Behavior is passed as values, not hardcoded into service internals.

## 8. Persistence Preparation

Persistence is intentionally prepared, not implemented:
- generic repository contract in `repository.Repository`
- current adapter in `repository.InMemoryRepository`
- identity contract in `domain.common.Identifiable<ID>`

This makes future database repositories swappable without rewriting domain logic or application workflows.

## 9. Future Backend Modularization

Planned evolution:
- add persistence adapters (e.g. JDBC/JPA) behind existing repository interfaces,
- keep service layer as use-case orchestration boundary,
- expose services via REST or other API interfaces,
- move `App.java` from demo script to integration test/demo harness.

Current package separation already supports this migration path.

## 10. Conclusion

The final codebase demonstrates OO concepts through real consulting-company workflows, not isolated textbook examples.  
Key design decision: preserve domain invariants inside domain classes, keep orchestration in services, and keep data access behind generic repositories.  
Concept coverage is explicit, traceable, and aligned with practical maintainability.

## Concept Mapping Table

| Concept | Implementation |
|---|---|
| Overloading | `Project.assignEmployee`, `Timesheet.addEntry`, `Contract.renew` |
| Coercion | Not explicitly implemented; intentionally skipped for design simplicity |
| Inclusion polymorphism | `Employee`, `PermanentEmployee`, `ContractEmployee`; payroll in `PayrollService` |
| Interface polymorphism | `Signable`, `Contract`, `Invoice`; signing in `SigningService` |
| Parametric polymorphism | `Repository<ID, T>`, `InMemoryRepository<ID, T>` |
| Generic class | `InMemoryRepository<ID, T>`, `ApprovalQueue<T>` |
| Generic method | `ReportService.filter`, `ReportService.map`, `SigningService.signAll` |
| Upper-bounded wildcard | `Collection<? extends Employee>` in `PayrollService` |
| Lower-bounded wildcard | `Collection<? super Employee>` in `EmployeeService.copyEmployees` |
| Generic collection | `List<T>`, `Map<ID, T>`, `Queue<T>` |
| Lambda function | Predicate/function usage in `App.java` reporting sections |
| Method references | `Employee::getFullName`, `Timesheet::getTotalHours` |
| Higher-order function | `ReportService` methods accepting `Predicate<T>` and `Function<T, R>` |
| Persistence preparation | Repository abstractions + `Identifiable<ID>` |
| Modularization | `domain`, `service`, `repository`, `workflow`, `app` packages |

## Notes on intentionally skipped ideas

- Dynamic typing is not introduced because Java is statically typed; forcing dynamic patterns would weaken type safety and code clarity.
- Additional artificial subclasses or toy containers (e.g. `Box<T>`) are intentionally avoided to keep demonstrations tied to real business needs.
