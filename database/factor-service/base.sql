CREATE USER if not exists 'adminCB'@'localhost' IDENTIFIED BY 'adminCB@2023';
GRANT ALL PRIVILEGES ON * . * TO 'adminCB'@'localhost';

CREATE DATABASE if not exists `cb_factor-service`
COLLATE utf8mb3_general_ci;



# --INSERT IGNORE INTO `users`
# --(`id`, `username`, `name`, `email`, `password`, `state`, `created_on`) VALUES
# --(1, 'admin', null, null, null, 1, null);
