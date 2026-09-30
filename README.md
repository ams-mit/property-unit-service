# property-unit-service

Group 2 owns the property inventory and the dedicated MySQL `property_db`. The service
listens on port 8082. See [GROUP2_UNIT_CONTRACT.md](GROUP2_UNIT_CONTRACT.md) for the unit
API used by `lease-occupancy-service`.

## Run with Docker Compose

From the parent `Backend` directory in PowerShell:

```powershell
$env:PROPERTY_DB_PORT = '3309'
docker compose -f .\property-unit-service\docker-compose.yml up -d --build
docker compose -f .\property-unit-service\docker-compose.yml ps
```

The database host port defaults to 3307. Set `PROPERTY_DB_PORT` to another free port if a
local MySQL instance already uses 3307. The app container always reaches its database as
`property_db:3306`; changing the host port does not affect container-to-container traffic.
For a direct Maven run, set `SPRING_DATASOURCE_URL` to match the chosen host port.

Check service health and Flyway's migration history:

```powershell
Invoke-RestMethod http://localhost:8082/actuator/health
docker compose -f .\property-unit-service\docker-compose.yml exec property_db mysql -uproperty_user -pproperty_pass property_db -e "SHOW TABLES; SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

The MySQL data lives in a named Docker volume. `docker compose down` stops and removes the
containers but retains that volume; do not use `down -v` if you want to keep the data.
Protected API endpoints require a Gateway-issued JWT and configured `GATEWAY_JWT_PUBLIC_KEY`.

## Run tests

```powershell
cd .\property-unit-service
mvn verify
```
