# Property Unit Service

Project A's property provider runs on port 8082 and owns buildings, floors, unit types, units, and ownership records in `property_unit_db`. It uses Java 21, Spring Boot 4.1.1, MySQL, Flyway, and Gateway-signed RS256 JWTs.

## Configuration

Copy `.env.example` to an untracked `.env` and set a real database password, the Gateway public key, this service's private key, and the Gateway URL. Private keys and real credentials must remain outside Git. `SPRING_DATASOURCE_URL` defaults to `jdbc:mysql://localhost:3307/property_unit_db`; the Docker deployment points it at the Compose MySQL container. Outbound validation uses `API_GATEWAY_URL`, a two-second connection timeout, a five-second read timeout, and propagates `X-Request-ID`.

```powershell
mvn clean test
mvn clean package
# With the environment configured:
java -jar target/property-unit-service-0.0.1-SNAPSHOT.jar
```

The service requires its own MySQL database. Flyway migrates the existing V1-V4 schema and V5 adds canonical UUIDs, codes, status fields, and uniqueness constraints. Hibernate validates the result. `/actuator/health` checks database connectivity. `/v3/api-docs` and `/swagger-ui.html` expose OpenAPI.

## API

The canonical domain routes are POST/GET `/api/v1/buildings`, `/api/v1/unit-types`, `/api/v1/units`, and `/api/v1/ownerships`, plus GET `/api/v1/internal/units/{unitId}/{exists|validate|ownership|status}`. Building creation accepts an optional `floors` list; the response returns their UUIDs. All JSON results use the shared success/error envelope. Collection results include `pagination`. Internal routes require a Gateway-issued service JWT from an explicitly allowed caller.

`POST /api/v1/ownerships` validates `ownerId` as a Resident Management resident profile UUID through `RES-INT-001` before saving. Validation failures return `503 DEPENDENCY_UNAVAILABLE`; a 404 response maps to `OWNER_NOT_FOUND`.

The fixed 12-route inventory does not define a floor update endpoint or a unit status transition endpoint. Existing routes outside that inventory are not registered by the canonical application. Owner and tenant unit/ownership searches fail closed until the Resident Management relationship response shape is agreed. `PROP-INT-002` includes unit type `capacity` for Lease Occupancy, as defined by the provider OpenAPI.

## Integration notes

The Gateway routes `/api/v1/buildings/**`, `/api/v1/unit-types/**`, `/api/v1/units/**`, `/api/v1/ownerships/**`, and `/api/v1/internal/units/**` here. Lease Occupancy consumes `PROP-INT-002` for unit state and capacity and `PROP-INT-003` for ownership. Lease status changes cannot update property unit status under the fixed contract; that cross-service behavior needs a shared contract decision. The property contract includes `utility-charge-service` as an internal caller, while the central registry omits it from the property consumer list.
