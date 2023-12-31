SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `u_company` (
                                         company_id varchar(36) NOT NULL UNIQUE,
                                         client_id varchar(36) NOT NULL UNIQUE,
                                         name varchar(128) NOT NULL,
                                         nationalCode INT(11) UNSIGNED NOT NULL,
                                         economicCode varchar(10) NOT NULL,
                                         unique_code varchar(10) NOT NULL,
                                         pk varchar(255) NOT NULL,
                                         legalType varchar(10) NOT NULL,
                                         tell varchar(10) NOT NULL,
                                         address varchar(255) NOT NULL,
                                         postCode varchar(10) NOT NULL,
                                         city_id varchar(36) NOT NULL,
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
