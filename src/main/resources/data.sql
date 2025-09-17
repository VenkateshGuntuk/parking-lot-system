insert into pricing_rule(type, free_minutes, rate_per_hour) values ('BIKE', 120, 10.00);
insert into pricing_rule(type, free_minutes, rate_per_hour) values ('CAR', 120, 20.00);
insert into pricing_rule(type, free_minutes, rate_per_hour) values ('TRUCK', 120, 40.00);

insert into parking_lot(location, floors) values ('Downtown', 3);

insert into gate(parking_lot_id, floor, type) values (1, 0, 'ENTRY');
insert into gate(parking_lot_id, floor, type) values (1, 1, 'ENTRY');

-- floor 0 (lot 1)
insert into parking_slot(floor, number, type, status, version, parking_lot_id) values (0, 1, 'CAR', 'AVAILABLE', 0, 1);
insert into parking_slot(floor, number, type, status, version, parking_lot_id) values (0, 2, 'CAR', 'AVAILABLE', 0, 1);
insert into parking_slot(floor, number, type, status, version, parking_lot_id) values (0, 101, 'BIKE', 'AVAILABLE', 0, 1);
insert into parking_slot(floor, number, type, status, version, parking_lot_id) values (0, 200, 'TRUCK', 'AVAILABLE', 0, 1);

-- floor 1 (lot 1)
insert into parking_slot(floor, number, type, status, version, parking_lot_id) values (1, 1, 'CAR', 'AVAILABLE', 0, 1);
insert into parking_slot(floor, number, type, status, version, parking_lot_id) values (1, 102, 'BIKE', 'AVAILABLE', 0, 1);
