INSERT INTO store(id, name, quantityProductsInStock) VALUES (1, 'TONSTAD', 10);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (2, 'KALLAX', 5);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE store_seq RESTART WITH 4;

INSERT INTO product(id, name, stock) VALUES (1, 'TONSTAD', 10);
INSERT INTO product(id, name, stock) VALUES (2, 'KALLAX', 5);
INSERT INTO product(id, name, stock) VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE product_seq RESTART WITH 4;

INSERT INTO warehouse_location_lock(location) VALUES ('ZWOLLE-001');
INSERT INTO warehouse_location_lock(location) VALUES ('ZWOLLE-002');
INSERT INTO warehouse_location_lock(location) VALUES ('AMSTERDAM-001');
INSERT INTO warehouse_location_lock(location) VALUES ('AMSTERDAM-002');
INSERT INTO warehouse_location_lock(location) VALUES ('TILBURG-001');
INSERT INTO warehouse_location_lock(location) VALUES ('HELMOND-001');
INSERT INTO warehouse_location_lock(location) VALUES ('EINDHOVEN-001');
INSERT INTO warehouse_location_lock(location) VALUES ('VETSBY-001');

INSERT INTO warehouse(
  id, businessUnitCode, active_business_unit_code, location,
  capacity, stock, createdAt, archivedAt, version)
VALUES (1, 'MWH.001', 'MWH.001', 'ZWOLLE-001', 100, 10, '2024-07-01', null, 0);
INSERT INTO warehouse(
  id, businessUnitCode, active_business_unit_code, location,
  capacity, stock, createdAt, archivedAt, version)
VALUES (2, 'MWH.012', 'MWH.012', 'AMSTERDAM-001', 50, 5, '2023-07-01', null, 0);
INSERT INTO warehouse(
  id, businessUnitCode, active_business_unit_code, location,
  capacity, stock, createdAt, archivedAt, version)
VALUES (3, 'MWH.023', 'MWH.023', 'TILBURG-001', 30, 27, '2021-02-01', null, 0);
ALTER SEQUENCE warehouse_seq RESTART WITH 4;
