-- Note: Units 101-105 were intentionally excluded from this seed script because 
-- no Unit entity or units table exists in this codebase as of this migration.

DELETE f FROM floors f JOIN buildings b ON f.building_id = b.id WHERE b.building_code = 'BLDG-A';
DELETE FROM buildings WHERE building_code = 'BLDG-A';

INSERT INTO buildings (building_code, name, address, created_at, updated_at) 
VALUES ('BLDG-A', 'Building A', '123 Main St', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

SET @building_id = LAST_INSERT_ID();

INSERT INTO floors (building_id, floor_number, floor_name) VALUES
(@building_id, 1, 'Floor 1'),
(@building_id, 2, 'Floor 2'),
(@building_id, 3, 'Floor 3');
