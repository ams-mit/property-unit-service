# Group 2 unit contract (draft for integration review)

`property-unit-service` owns the unit inventory and MySQL `property_db`. Its database IDs
remain numeric because ownership rows already reference `units.id`. Other services use the
stable `units.public_id` UUID exposed as `unitId`; they never access the property database.

All internal calls go through the API Gateway with a Gateway-signed Service JWT. The three
endpoints below accept only `sub=lease-occupancy-service`. Confirm the Gateway routes and
register both services' keys before integrated use.

| Method | Path | Success data | Errors |
| --- | --- | --- | --- |
| GET | `/api/v1/internal/units/{unitId}` | `unitId`, `status`, `capacityLimit`, `ownerId` | 401/403, 404 |
| GET | `/api/v1/internal/units/{unitId}/capacity` | `unitId`, `capacityLimit` | 401/403, 404 |
| PATCH | `/api/v1/internal/units/{unitId}/status` | Unit details | 401/403, 404, 409, 422 |

Success responses use `{ "success": true, "data": { ... } }`. Status PATCH accepts
`{"status":"OCCUPIED"}` or `{"status":"AVAILABLE"}`. A unit in
`UNDER_MAINTENANCE` or `INACTIVE` cannot be changed by the lease service. The property
service provides `POST /api/v1/units` for managers to create a unit and receive its UUID.

The owner ID is nullable when no active ownership exists. Existing ownership records store
their owner IDs as strings; valid UUID strings are returned as `ownerId`. Group 1 must confirm
the exact owner/user identifier and validation contract before owner access is demonstrated.

Open integration decisions: Gateway route mapping, exact identity endpoint and roles,
maintenance-triggered relocation, and reconciliation when a remote status change succeeds
but the local lease transaction later fails.
