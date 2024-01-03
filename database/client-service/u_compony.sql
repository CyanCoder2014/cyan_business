SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `u_company` (
                                         company_id varchar(36) NOT NULL UNIQUE,
                                         client_id varchar(36) NOT NULL,
                                         name varchar(128) NULL DEFAULT NULL,
                                         national_code INT(11) UNSIGNED NOT NULL UNIQUE,
                                         economic_code varchar(36) NULL DEFAULT NULL,
                                         unique_code varchar(64) NULL DEFAULT NULL UNIQUE,
                                         pk varchar(255) NULL DEFAULT NULL,
                                         legal_type varchar(10) NULL DEFAULT NULL,
                                         tell varchar(10) NULL DEFAULT NULL,
                                         address varchar(255) NULL DEFAULT NULL,
                                         post_code varchar(10) NULL DEFAULT NULL,
                                         city_id varchar(36) NULL DEFAULT NULL,
                                         note varchar(255) NULL DEFAULT NULL,
                                         state varchar(10) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT company_id PRIMARY KEY (company_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `u_company`
    ADD KEY if not exists `u_company_company_id_index` (`company_id`),
    ADD KEY if not exists `u_company_client_id_index` (`client_id`)
