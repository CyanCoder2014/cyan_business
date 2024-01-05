SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `b_buyer` (
                                         buyer_id varchar(36) NOT NULL UNIQUE,
                                         national_code INT(128) UNSIGNED NOT NULL,
                                         economic_code varchar(64) NOT NULL,
                                         buyer_type varchar(10) NOT NULL,
                                         tell varchar(64) NOT NULL,
                                         address varchar(255) NOT NULL,
                                         post_code varchar(10) NOT NULL,
                                         city_id varchar(36) NOT NULL,
                                         note varchar(255) NULL DEFAULT NULL,
                                         state varchar(10) NULL DEFAULT NULL,
                                         client_id varchar(36) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT buyer_id PRIMARY KEY (buyer_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `b_buyer`
    ADD KEY if not exists `b_buyer_city_id_index` (`city_id`)
