CREATE TABLE units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    floor_id BIGINT NOT NULL,
    unit_type_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'AVAILABLE' NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_unit_building FOREIGN KEY (building_id) REFERENCES buildings(id),
    CONSTRAINT fk_unit_floor FOREIGN KEY (floor_id) REFERENCES floors(id),
    CONSTRAINT fk_unit_type FOREIGN KEY (unit_type_id) REFERENCES unit_types(id)
);
