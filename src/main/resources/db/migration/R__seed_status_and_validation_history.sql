-- This script intentionally excludes INACTIVE, which exists in code but wasn't requested by the ticket.

-- Ensure a default UnitType exists with capacityLimit > 0 (for OCCUPIED validation logic)
INSERT INTO unit_types (type_name, base_rent, capacity_limit, amenities_summary)
SELECT 'Standard 1B1B', 1500.00, 2, 'Basic amenities'
WHERE NOT EXISTS (SELECT 1 FROM unit_types WHERE type_name = 'Standard 1B1B');

SET @type_id = (SELECT id FROM unit_types WHERE type_name = 'Standard 1B1B' LIMIT 1);

-- Get Building A
SET @building_id = (SELECT id FROM buildings WHERE building_code = 'BLDG-A');

-- Get Floors 1, 2, and 3
SET @floor_1_id = (SELECT id FROM floors WHERE building_id = @building_id AND floor_number = 1 LIMIT 1);
SET @floor_2_id = (SELECT id FROM floors WHERE building_id = @building_id AND floor_number = 2 LIMIT 1);
SET @floor_3_id = (SELECT id FROM floors WHERE building_id = @building_id AND floor_number = 3 LIMIT 1);

-- Idempotent DELETE-then-INSERT for units
DELETE FROM units WHERE unit_number IN ('TEST-A101', 'TEST-A201', 'TEST-A301');

INSERT INTO units (public_id, floor_id, unit_type_id, unit_number, status) VALUES
(UNHEX(REPLACE('11111111-1111-1111-1111-111111111111', '-', '')), @floor_1_id, @type_id, 'TEST-A101', 'AVAILABLE'),
(UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), @floor_2_id, @type_id, 'TEST-A201', 'OCCUPIED'),
(UNHEX(REPLACE('33333333-3333-3333-3333-333333333333', '-', '')), @floor_3_id, @type_id, 'TEST-A301', 'UNDER_MAINTENANCE');
