CREATE USER if not exists 'adminCB'@'localhost' IDENTIFIED BY 'adminCB@2023';
GRANT ALL PRIVILEGES ON * . * TO 'adminCB'@'localhost';

CREATE DATABASE if not exists `cb_buyer_service`
COLLATE utf8mb3_general_ci;

