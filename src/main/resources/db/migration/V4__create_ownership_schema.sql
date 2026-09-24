CREATE TABLE ownerships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    share_percentage DECIMAL(5,2) NOT NULL CHECK (share_percentage >= 0 AND share_percentage <= 100.00),
    start_date DATE NOT NULL,
    end_date DATE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_ownerships_units FOREIGN KEY (unit_id) REFERENCES units(id)
);
