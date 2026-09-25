# Known Gaps

## 1. Missing Auto-Floor Generation
* **What's missing**: When a new `Building` is created, the system should automatically generate the requested number of floors for that building. Currently, the controller just saves the building without generating any floors.
* **Files it would touch**: `BuildingController.java` (or ideally a new `BuildingService.java` to handle the business logic).
* **Existing test**: `BuildingControllerTest.shouldAutoGenerateFloorsOnBuildingCreation` currently encodes this expected behavior (and fails because the feature is missing).

## 2. Missing Duplicate Building Code Handling
* **What's missing**: The system does not gracefully handle duplicate building codes. The database has a `UNIQUE` constraint on `building_code`, which surfaces as an unhandled 500 Internal Server Error instead of the expected 409 Conflict response.
* **Files it would touch**: `BuildingController.java` and potentially a new `@ControllerAdvice` / `GlobalExceptionHandler` to catch the constraint violation and return a 409.
* **Existing test**: `BuildingControllerTest.shouldRejectDuplicateBuildingCode` currently encodes this expected behavior (and fails because the feature is missing).

## 3. Missing Unit Entity and Table
* **What's missing**: The application lacks a `Unit` entity and corresponding `units` database table. Only `UnitType` currently exists.
* **Files it would touch**: A new `Unit.java` entity class, a new Flyway migration (e.g., `V3__create_units_schema.sql`), and optionally repository/controller files for Unit management.
* **Existing test**: None currently exists for this specific feature.
