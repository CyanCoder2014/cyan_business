SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_units` (
                                         unit_id varchar(36) NOT NULL UNIQUE,
                                         code varchar(128) NULL DEFAULT NULL,
                                         name varchar(128) NOT NULL,
                                         state varchar(10) NULL DEFAULT NULL,
                                         client_id varchar(36) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT unit_id PRIMARY KEY (unit_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_units`
    ADD KEY if not exists `f_units_unit_id_index` (`unit_id`)
