CREATE TABLE unit_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(255) NOT NULL,
    base_rent DECIMAL(10,2) NOT NULL CHECK (base_rent >= 0),
    capacity_limit INT NOT NULL CHECK (capacity_limit >= 0),
    amenities_summary TEXT
);
