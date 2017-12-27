DROP DATABASE IF EXISTS hecares;
CREATE DATABASE hecares CHARACTER SET utf8;
DROP USER IF EXISTS 'hecares'@'localhost';
CREATE USER 'hecares'@'localhost' IDENTIFIED BY 'hecares';
GRANT ALL PRIVILEGES ON hecares.* TO 'hecares'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
