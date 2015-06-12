--
-- Sample dataset

INSERT INTO user (id, serial_id, installation_id, username, active, new_user, pin, creation_time, modification_time) VALUES (1, 1, 'testInstallation1', 'my active User', 1, 0, '9427', '2015-06-10 10:10:10', '2015-06-10 10:10:10');
INSERT INTO user (id, serial_id, installation_id, username, active, new_user, pin, creation_time, modification_time) VALUES (2, 2, 'testInstallation2', 'my inactive User', 0, 1, NULL, '2015-06-10 10:10:10', '2015-06-10 10:10:10');
