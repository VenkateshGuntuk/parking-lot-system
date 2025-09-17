# Parking Lot Management System - Design & Implementation Overview

This document maps each requirement to the implemented classes, explains behavior, discusses alternatives, and justifies design choices.

## 1) Authentication & Authorization (Google OAuth2 + Roles)
- Key Classes/Files:
  - `com.example.parking.security.SecurityConfig`
  - `src/main/resources/application.yml` (OAuth2 client config)
- What it does:
  - Uses Spring Security OAuth2 OIDC login with Google as the provider.
  - Assigns roles based on email: all users get `ROLE_USER`; emails listed in `ADMIN_EMAILS` also get `ROLE_ADMIN`.
  - Secures APIs: `/api/**` requires authentication; admin-only endpoints under `/api/admin/**` require `ROLE_ADMIN` (reinforced via `@PreAuthorize`).
- Alternatives considered:
  - JWT resource server vs. OAuth2 login: chose OAuth2 login for simplicity and alignment with requirement.
  - Role mapping via custom DB table: chose env-based email allowlist for speed; can be replaced with DB/IdP claims later.
- Rationale:
  - Minimal configuration footprint, easy local testing, clear separation of USER vs ADMIN.

## 2) Domain Model (Entities) & Persistence (JPA + H2)
- Entities:
  - `ParkingLot` (id, location, floors)
  - `Gate` (id, parkingLot, floor, type)
  - `ParkingSlot` (id, floor, number, type, status, version, parkingLot)
  - `Vehicle` (id, plateNo [unique], type, ownerEmail)
  - `Ticket` (id, vehicle, slot, entryTime, exitTime, status, version)
  - `Payment` (id, ticket, amount, status, timestamp)
  - `PricingRule` (id, type, freeMinutes, ratePerHour)
- Repositories (DAO):
  - `ParkingLotDao`, `GateDao`, `ParkingSlotDao`, `VehicleDao`, `TicketDao`, `PaymentDao`, `PricingRuleDao`
- What it does:
  - Models multi-floor lot, gates, typed slots, and ticket/payment lifecycle.
  - Enforces uniqueness on `Vehicle.plateNo` and uses optimistic locking (`@Version`) on `ParkingSlot` and `Ticket`.
- Alternatives considered:
  - Separate `SlotSize` entity: replaced by `SlotType` enum to match requirements while staying simple.
  - Soft deletes vs. hard deletes for slots: kept simple with hard deletes; production might prefer soft deletes.
- Rationale:
  - Matches assignment entities; expandable; uses H2 in-memory DB for quick demo.

## 3) Slot Allocation Strategy (Nearest, Gate-aware) & Concurrency
- Key Classes:
  - `AllocationService` (interface)
  - `NearestAllocationStrategy` and `LevelWiseAllocationStrategy` (impls)
  - `ParkingSlotDao.findNearestByGate(...)` (PESSIMISTIC_WRITE)
  - `ParkingSlotDao.markOccupiedIfAvailable(...)` (conditional update)
- What it does:
  - Strategy interface abstracts allocation; the default picks nearest slot by absolute floor distance from the given gate floor; a level-wise variant is available (floor asc, number asc).
  - Uses PESSIMISTIC_WRITE during selection, then a conditional update `markOccupiedIfAvailable` to atomically claim the slot under contention.
  - On successful claim (rowsAffected == 1), the slot is considered reserved/occupied within the same transaction.
- Alternatives considered:
  - Optimistic locking only: may cause more retries under contention.
  - Queue-based allocator (in-memory or Redis): higher complexity; unnecessary for assignment scale.
  - Strategy variants (random, round-robin, level-wise): easily added by implementing `AllocationService`.
- Rationale:
  - DB locking provides a simple and robust guarantee against double booking across multiple app instances.

Config:
- Strategy is configurable via `app.allocation.strategy` (defaults to `nearestStrategy`).
- Available beans: `nearestStrategy`, `levelWiseStrategy`.

## 4) Ticket Lifecycle (Entry, Exit, Receipt) & Duplicate Prevention
- Key Classes:
  - `TicketService` (interface), `TicketServiceImpl` (impl)
  - `TicketDao`, `VehicleDao`
  - DTOs: `EntryRequest`, `EntryResponse`, `ExitRequest`, `ExitResponse`, `ReceiptDto`
  - Controller: `TicketController`
- What it does:
  - Entry: validates no active ticket for plate; creates/loads `Vehicle`; allocates slot via `AllocationService`; creates `Ticket` with `entryTime` and `ACTIVE` status; returns `EntryResponse` with ticket and slot details.
  - Amount Preview: calculates price based on current duration and `PricingRule`.
  - Exit: calculates amount; initiates payment; marks `Ticket` as `PAID` with `exitTime`; frees slot.
  - Receipt: returns details for the stay and computed amount.
- Alternatives considered:
  - Store computed amount on `Ticket` at exit to preserve billed total: can be added easily; current approach recalculates on demand.
  - Enforce unique active ticket per plate with DB constraint: chosen application-level check for demo; DB constraint can complement.
- Rationale:
  - Keeps flow simple and strictly transactional; meets duplicate prevention and receipt generation needs.

## 5) Pricing Rules & Payment Atomicity
- Key Classes:
  - `PricingService` (interface), `PricingServiceImpl` (impl)
  - `PaymentService` (interface), `PaymentServiceImpl` (impl)
  - `PricingRuleDao`, `PaymentDao`
- What it does:
  - Pricing: DB-configurable rules per vehicle type with free minutes and hourly rate (fallback defaults if none).
  - Payment: records `INITIATED`, calls gateway (simulated), then persists `SUCCESS` or `FAILED`.
  - On success: within a transaction, ticket is set `PAID` with `exitTime` and slot is freed; on failure: payment is `FAILED`, no slot free.
- Alternatives considered:
  - External gateway integration with webhooks: overkill for assignment; ready to plug via `PaymentService`.
  - Tiered pricing by floor/time-of-day: can extend `PricingRule` without changing service boundary.
- Rationale:
  - Demonstrates atomicity, extensible rules, and clean separation of concerns.

## 6) Admin Management (Slots & Pricing)
- Key Classes:
  - `AdminController`
  - `ParkingSlotDao`, `PricingRuleDao`
- What it does:
  - Slots: add/list/delete with type and coordinates (floor, number) within a lot.
  - Pricing: upsert/list rules per vehicle type.
  - All endpoints require `ROLE_ADMIN`.
- Alternatives considered:
  - Separate admin service layer: current controller calls DAOs directly for brevity; can add service if logic grows.
- Rationale:
  - Meets requirement to configure capacity and pricing with minimal complexity.

## 7) Error Handling & Edge Cases
- Key Classes:
  - `GlobalExceptionHandler` (maps errors to structured responses)
  - Custom exceptions: `DuplicateEntryException`, `NoSlotAvailableException` (available for future explicit use)
- What it does:
  - Returns `400/409/404` with JSON error body.
  - Duplicate entry prevented; full lot returns an error; unauthorized blocked by security.
- Alternatives considered:
  - Problem+JSON (RFC7807): could be added; current simple JSON suffices.
- Rationale:
  - Clear, user-friendly errors with minimal boilerplate.

## 8) Multiple Entry Gates & Consistent Allocation
- Implementation:
  - `Gate` models entrances with a floor in a specific `ParkingLot`.
  - `EntryRequest` includes `parkingLotId`, `gateId`, and `gateFloor`.
  - Allocation query orders by `abs(s.floor - :gateFloor)` then `floor/number` to choose nearest.
  - PESSIMISTIC_WRITE lock ensures consistent allocation across concurrent entries.
- Alternatives:
  - Distance matrices per gate/slot, or zones: can be layered into strategy.
- Rationale:
  - Simple and effective nearest strategy; easy to replace.

## 9) Data Initialization & Testing Aids
- Files:
  - `application.yml` uses `ddl-auto: update`, `defer-datasource-initialization: true`, `spring.sql.init.mode: always`.
  - `data.sql` seeds: pricing rules, one lot, two gates, and per-floor slots.
  - Postman: `postman/parking-lot.postman_collection.json` with updated request bodies.
- Why:
  - Ensures DB schema is created before running seeds; enables immediate end-to-end testing.

## 10) Extensibility & Alternatives Summary
- Strategy pattern at allocation; can add gate-aware heuristics, reservations, VIP lanes.
- Pricing rules are DB-backed; can add admin UI, time-of-day tiers, surge pricing.
- Payments can integrate with a gateway; add retry/compensation workflows.
- Add `ParkingLot`-level capacity analytics and reporting.

## 11) How to Run & Test
- Prereqs: JDK 17; optional env: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `ADMIN_EMAILS`.
- Run: `./mvnw spring-boot:run`
- H2 console: `/h2-console` (jdbc url: `jdbc:h2:mem:parkingdb`)
- Postman: import `postman/parking-lot.postman_collection.json`.
  - Use "Ticket - Enter (gate-aware)" with body:
    `{ "plateNo": "KA01AB1234", "vehicleType": "CAR", "ownerEmail": "user@example.com", "parkingLotId": 1, "gateId": 1, "gateFloor": 0 }`
  - Switch allocation strategy: set env `ALLOCATION_STRATEGY=levelWiseStrategy` before run

## 12) Functional Flow Sequences

### A) Vehicle Entry (Gate-aware)
1. Client calls `POST /api/tickets/enter` with `plateNo`, `vehicleType`, `ownerEmail`, `parkingLotId`, `gateId`, `gateFloor`.
2. `TicketController.enter` → `TicketService.enter`.
3. `TicketService.enter`:
   - Checks `TicketDao.findActiveByPlate` → reject if existing active.
   - Loads/creates `Vehicle` via `VehicleDao`.
   - Calls `AllocationService.allocate(parkingLotId, gateId, gateFloor, vehicleType)`.
4. `AllocationServiceImpl.allocate`:
   - `ParkingSlotDao.findNearestByGate(lotId, gateId, gateFloor, type)` with `PESSIMISTIC_WRITE`.
   - Attempts to claim via `markOccupiedIfAvailable(slot.id)`; if rowsAffected==1, the slot is claimed.
5. `TicketService.enter` creates `Ticket(entryTime=now,status=ACTIVE)` and returns `EntryResponse(ticketId, slotId, floor, number, plateNo, entryTime)`.

**DB Changes:**
- `SELECT` from `ticket` WHERE `vehicle_id` IN (SELECT `id` FROM `vehicle` WHERE `plate_no` = ?) AND `status` IN ('ACTIVE','PAYMENT_PENDING')
- `SELECT` from `vehicle` WHERE `plate_no` = ? (if not found, `INSERT` into `vehicle` with `plate_no`, `type`, `owner_email`)
- `SELECT ... FOR UPDATE` from `parking_slot` WHERE `parking_lot_id` = ? AND `type` = ? AND `status` = 'AVAILABLE' ORDER BY ABS(`floor` - ?), `floor`, `number` (using gateId for future gate-specific logic)
- Conditional claim: `UPDATE parking_slot SET status = 'OCCUPIED' WHERE id = ? AND status = 'AVAILABLE'` (rowsAffected==1)
- `INSERT` into `ticket` with `vehicle_id`, `slot_id`, `entry_time`, `status = 'ACTIVE'`

### B) Amount Preview
1. Client calls `GET /api/tickets/amount/{ticketId}`.
2. `TicketController.preview` → `TicketService.receipt` (read-only computation).
3. `TicketService.receipt`:
   - Loads `Ticket`; computes `Duration` from `entryTime` to now; calls `PricingService.calculateAmount`.
   - Returns `ReceiptDto` (entry/exit if present, amount).
4. Controller responds with `amount` from `ReceiptDto`.

**DB Changes:**
- `SELECT` from `ticket` WHERE `id` = ? (loads ticket with vehicle and slot details)
- `SELECT` from `pricing_rule` WHERE `type` = ? (for vehicle type pricing)
- No writes (read-only operation)

### C) Pay & Exit
1. Client calls `POST /api/tickets/exit` with `{ ticketId }`.
2. `TicketController.payAndExit` → `TicketService.payAndExit` (transactional).
3. `TicketService.payAndExit`:
   - Loads `Ticket`; computes `Duration`; gets amount via `PricingService`.
   - Calls `PaymentService.initiate(ticketId, amount)` which records `INITIATED`, calls gateway (simulated), then sets `SUCCESS` or `FAILED`.
   - On success: sets `Ticket.status=PAID`, `exitTime=now`, saves, then frees slot.
   - On failure: returns FAILED; slot remains occupied.
4. Returns `ExitResponse(ticketId, amount, "SUCCESS")`.
5. If payment fails (throw before success), transaction rolls back and slot remains `OCCUPIED`.

**DB Changes:**
- `SELECT` from `ticket` WHERE `id` = ? (loads ticket with vehicle and slot)
- `SELECT` from `pricing_rule` WHERE `type` = ? (for amount calculation)
- `SELECT` from `payment` WHERE `ticket_id` = ? (check existing payment)
- `INSERT` or `UPDATE payment` SET `ticket_id` = ?, `amount` = ?, `status` IN ('INITIATED','SUCCESS','FAILED'), `timestamp` = NOW()
- `UPDATE ticket SET status = 'PAID', exit_time = NOW(), version = version + 1 WHERE id = ?`
- `UPDATE parking_slot SET status = 'AVAILABLE' WHERE id = ?`
- If payment fails before success status, all changes roll back

### D) Admin: Manage Slots
1. Add: `POST /api/admin/slots?floor&number&type` → creates `ParkingSlot(status=AVAILABLE, parkingLot=<set via future param or default>)`.
2. List: `GET /api/admin/slots` → lists all slots.
3. Delete: `DELETE /api/admin/slots/{id}` → removes slot (hard delete for demo).

**DB Changes:**
- Add: `INSERT` into `parking_slot` with `floor`, `number`, `type`, `status = 'AVAILABLE'`, `parking_lot_id`
- List: `SELECT` from `parking_slot` (all slots)
- Delete: `DELETE` from `parking_slot` WHERE `id` = ?

### E) Admin: Manage Pricing
1. Upsert: `POST /api/admin/pricing?type&freeMinutes&ratePerHour` → creates/updates `PricingRule`.
2. List: `GET /api/admin/pricing` → lists all pricing rules.

**DB Changes:**
- Upsert: `SELECT` from `pricing_rule` WHERE `type` = ? (if exists, `UPDATE`; else `INSERT`)
- List: `SELECT` from `pricing_rule` (all rules)

### F) Concurrency & Double Allocation Prevention
1. Two clients call `enter` concurrently for same type.
2. Both reach `ParkingSlotDao.findNearestByGate(...)` with `PESSIMISTIC_WRITE`.
3. DB lock serializes selection; only one request updates a given slot to `OCCUPIED` first.
4. The second request sees the next available slot or fails with "full" if none remain.

**DB Changes:**
- Concurrent `SELECT ... FOR UPDATE` from `parking_slot` WHERE `parking_lot_id` = ? AND `type` = ? AND `status` = 'AVAILABLE' ORDER BY ABS(`floor` - ?), `floor`, `number` (gateId available for future enhancements)
- First transaction: `UPDATE parking_slot SET status = 'OCCUPIED', version = version + 1 WHERE id = ?`
- Second transaction: sees different slot (next available) or no results (lot full)
- Pessimistic lock ensures only one transaction can modify a slot at a time
