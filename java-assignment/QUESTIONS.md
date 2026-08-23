# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, but incrementally. Product and Store use Panache's active-record style directly
from their REST resources, while Warehouse has a domain model, ports and a repository,
and Location is an in-memory gateway. The first style is concise for CRUD, but it binds
HTTP handling, transaction boundaries and persistence together. The Warehouse style is
more verbose, but its business rules can be tested without Quarkus or a database.

I would converge business-heavy areas on application services/use cases plus repository
ports, keeping JPA entities inside persistence adapters. Product could remain simple
until its rules justify that separation. I would also place transaction boundaries at
the use-case level, standardize error mapping, and enforce invariants in both application
logic and database constraints where possible.

For legacy synchronization, an after-success CDI event is sufficient for this exercise:
it prevents publishing rolled-back data. In production I would prefer a transactional
outbox with retries, idempotency and monitoring, because an external call made after a
commit can still fail and cannot be rolled back with the database transaction.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Contract-first OpenAPI makes the API reviewable before implementation, provides a single
source for generated server/client types, and helps prevent accidental breaking changes.
It is especially useful when several teams or external consumers integrate with the API.
Its costs are generator configuration, generated-code noise, and occasional friction when
the generated signatures do not express framework behavior well (for example response
status handling). Generated transport models must also not become domain models.

Code-first endpoints are quicker and easier to debug for a small internal CRUD API, and
the implementation can use the framework naturally. The risk is documentation drift and
inconsistent naming, errors and status codes unless an OpenAPI document is generated and
checked in CI.

I would use contract-first OpenAPI for Warehouse and other shared/public APIs, generate
only the transport boundary, and map it to domain objects. For small private endpoints I
would accept code-first if CI publishes and validates the generated specification. The
important choice is a consistent contract and compatibility policy, not generation alone.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritize tests by business and operational risk:

1. Fast unit tests for location resolution and warehouse invariants: duplicate business
   unit codes, unknown locations, warehouse-count and aggregate-capacity limits, stock
   versus capacity, replacement stock matching, and preservation of archived history.
2. Repository integration tests for active-versus-archived queries and atomic replacement.
   H2 gives fast local feedback, while a smaller PostgreSQL suite in CI should catch SQL,
   transaction and schema differences.
3. REST tests for the happy-path lifecycle and the important 400/404/409 responses.
4. Transaction tests proving legacy synchronization happens only after a successful
   commit. A production outbox would additionally need retry, idempotency and recovery
   tests, plus a contract test against the legacy boundary.

I would avoid asserting implementation details. CI would run unit and H2 tests on every
change, PostgreSQL and contract tests before merge, and a small end-to-end smoke suite on
deployment. Coverage reports are useful for finding untested branches, but risk-based
scenarios, regression tests for every defect, stable test data, and periodic mutation-test
sampling provide stronger confidence than a percentage target alone.
```
