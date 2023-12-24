CREATE USER if not exists 'adminCB'@'localhost' IDENTIFIED BY 'adminCB@2023';
GRANT ALL PRIVILEGES ON * . * TO 'adminCB'@'localhost';

CREATE DATABASE if not exists `cb_factor-service`
COLLATE utf8mb3_general_ci;

SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_factors` (
    id INT(11) NOT NULL AUTO_INCREMENT,
    code varchar(10) NOT NULL,
    factor_date timestamp NULL DEFAULT NULL,
    seller_id INT(11) UNSIGNED NULL DEFAULT NULL,
    buyer_id INT(11) UNSIGNED NULL DEFAULT NULL,
    pay_state varchar(10) NULL DEFAULT NULL,
    payed DOUBLE NULL DEFAULT NULL,
    note varchar(255) NULL DEFAULT NULL,
    state varchar(10) NULL DEFAULT NULL,
    created_by INT(11) UNSIGNED NULL DEFAULT NULL,
    updated_by INT(11) UNSIGNED NULL DEFAULT NULL,
    created_at timestamp NULL DEFAULT NULL,
    updated_at timestamp NULL DEFAULT NULL,
    deleted_at timestamp NULL DEFAULT NULL,
    status  varchar(10) NULL DEFAULT NULL,

    CONSTRAINT id PRIMARY KEY (id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_factors`
  ADD KEY if not exists `f_factors_seller_id_index` (`seller_id`),
  ADD KEY if not exists `f_factors_buyer_id_index` (`buyer_id`);


--INSERT IGNORE INTO `users`
--(`id`, `username`, `name`, `email`, `password`, `state`, `created_on`) VALUES
--(1, 'admin', null, null, null, 1, null);
