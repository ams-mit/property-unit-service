CREATE TABLE units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id BINARY(16) NOT NULL UNIQUE,
    building_id BIGINT,
    floor_id BIGINT NOT NULL,
    unit_type_id BIGINT NOT NULL,
    unit_number VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_units_floor_number UNIQUE (floor_id, unit_number),
    CONSTRAINT fk_units_building FOREIGN KEY (building_id) REFERENCES buildings(id),
    CONSTRAINT fk_units_floor FOREIGN KEY (floor_id) REFERENCES floors(id),
    CONSTRAINT fk_units_type FOREIGN KEY (unit_type_id) REFERENCES unit_types(id),
    CONSTRAINT chk_units_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'UNDER_MAINTENANCE', 'INACTIVE'))
);