INSERT INTO buildings (building_code, name, address, created_at, updated_at) 
VALUES ('BLDG-A', 'Building A', '123 Main St', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), updated_at = CURRENT_TIMESTAMP;

SET @building_id = (SELECT id FROM buildings WHERE building_code = 'BLDG-A');

INSERT INTO floors (building_id, floor_number, floor_name)
SELECT @building_id, 1, 'Floor 1' WHERE NOT EXISTS
    (SELECT 1 FROM floors WHERE building_id = @building_id AND floor_number = 1);
INSERT INTO floors (building_id, floor_number, floor_name)
SELECT @building_id, 2, 'Floor 2' WHERE NOT EXISTS
    (SELECT 1 FROM floors WHERE building_id = @building_id AND floor_number = 2);
INSERT INTO floors (building_id, floor_number, floor_name)
SELECT @building_id, 3, 'Floor 3' WHERE NOT EXISTS
    (SELECT 1 FROM floors WHERE building_id = @building_id AND floor_number = 3);
