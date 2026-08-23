# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

I would start by defining which decisions the cost model must support. Financial posting,
operational optimization and customer/product profitability need different granularity and
latency. Direct costs such as warehouse labor or a carrier invoice should be captured at
source. Shared costs such as rent, regional management and technology need transparent,
versioned allocation drivers—for example occupied space, labor hours, orders, weight,
distance or pallet-days—rather than arbitrary percentages.

The model needs stable identifiers for warehouse/store, product, order and accounting
period; an auditable mapping to the chart of accounts; currency and tax treatment; and
rules for accruals, corrections, returns and late invoices. I would retain raw cost events
and allocation versions so totals can be reproduced and reconciled to the general ledger.
Useful outputs include cost per order/line/unit, cost by activity, variance to plan and
cost-to-serve by channel or customer.

Key questions are: Which system owns each cost? What accuracy and close-time SLA is
required? Which costs are controllable locally? How are shared costs allocated today?
At what grain are labor, inventory and transport events available? What reconciliation
tolerance is acceptable, and who approves allocation-rule changes?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

I would first establish a trustworthy baseline of volume, cost and service KPIs by site,
activity and order profile. Likely levers include demand-based labor scheduling, slotting
fast-moving products closer to dispatch, reducing pick travel and rework, improving
inventory placement between warehouses, consolidating shipments, optimizing carrier and
service-level selection, right-sizing packaging, reducing energy use, and selectively
automating stable high-volume processes.

Initiatives should be scored by expected annual impact, evidence/confidence, implementation
effort, time to value and operational risk. I would pilot the highest-value reversible
changes at a representative site, with a control baseline and guardrails for on-time
delivery, accuracy, damage/return rates, safety and customer satisfaction. Results should
be measured after accounting for volume and mix changes, then standardized and rolled out
in stages. Expected outcomes are lower unit cost and waste, better asset utilization and
more predictable capacity without degrading service.

I would ask where cost and service variance is greatest, which contractual or labor
constraints apply, what peak-season capacity must be protected, how benefits will be
attributed, and who owns adoption after a pilot.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

Integration prevents the operational tool and finance from producing competing versions
of cost. It enables faster close, current budget-versus-actual reporting, traceability from
an operational activity to a financial posting, and earlier detection of missing or
misclassified costs.

I would define a canonical cost-event contract with stable source IDs, business-unit and
account mappings, event time, accounting period, amount/currency and correction semantics.
Events or CDC can provide low-latency updates, while APIs or scheduled files may remain
appropriate for systems that do not support streaming. Consumers must be idempotent;
events should be schema-versioned, ordered where required, retried through a dead-letter
process, and protected with least-privilege access and encryption. A reconciliation job
should compare event counts and control totals with finance and explicitly manage late
arrivals and backdated adjustments. Dashboards and alerts should expose freshness,
failures and reconciliation differences.

Before selecting "real time" everywhere, I would ask which decisions truly require it,
which finance platform is authoritative, what posting and period-close rules apply, what
interfaces already exist, expected volume/availability SLAs, and how master-data changes
are governed.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Budgeting turns expected demand into labor, space, inventory, transport and cash needs;
forecasting updates those expectations as actual demand and costs arrive. Together they
support staffing and carrier commitments, capacity decisions, early variance action and
credible financial planning.

I would use a driver-based model: orders and lines by channel, SKU and handling profile,
seasonality and promotions, productivity rates, storage days, transport zones, wage and
carrier contracts, energy/fuel indices, inflation and currency. Assumptions must be named,
owned, versioned and time-effective. The system should support baseline, upside/downside
and disruption scenarios; rolling forecasts; approvals; and drill-down from company totals
to site and cost driver. Actuals should be compared at the same grain, with variance split
into volume, rate, efficiency and mix effects. Forecast accuracy and bias should be tracked
over time rather than hiding uncertainty behind a single number.

Questions include the planning horizon and refresh cadence, required granularity, peak and
service constraints, authoritative demand forecast, treatment of new sites/products, who
can override assumptions, and which decisions or approval thresholds each forecast drives.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

The Business Unit Code is a continuity key for reporting, but it must not be the only
identity of a physical warehouse. Each warehouse incarnation should have an immutable ID
and effective start/archive timestamps. Replacement should atomically archive the old
record and create the new active record with the same Business Unit Code; historical cost
events must continue pointing to the old immutable ID. No old transactions or allocations
should be rewritten.

Preserving history provides an audit trail, supports financial reconciliation, and creates
a baseline for validating the replacement business case. It also separates recurring run
cost from transition costs such as construction, automation, stock transfer, temporary
double running, write-offs, contract termination and ramp-up inefficiency. I would budget
those separately, define approval thresholds and contingency, and monitor committed,
accrued and actual spend alongside service, capacity and productivity during migration.
After go-live, like-for-like cost variance against the old operation and approved model
would show whether savings are real or caused by volume/mix changes.

Key questions are the cutover/effective date, whether sites overlap, ownership of in-flight
orders and stock, accounting treatment of transition assets and liabilities, historical
retention requirements, rollback criteria, and who approves cost or timeline variance.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
