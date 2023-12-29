SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `t_factor_tax` (
                                         factor_tax_id INT(11) UNSIGNED NOT NULL UNIQUE,
                                         factor_id varchar(128) NOT NULL UNIQUE,

                                         tax_api_uid_first varchar(128) NOT NULL,
                                         tax_api_reference_first varchar(128) NOT NULL,

                                         tax_api_uid_last varchar(128) NOT NULL,
                                         tax_api_reference_last varchar(128) NOT NULL,

                                         tax_api_correction_uid varchar(128) NOT NULL,
                                         tax_api_correction_reference varchar(128) NOT NULL,

                                         tax_api_cancellation_uid varchar(128) NOT NULL,
                                         tax_api_cancellation_reference varchar(128) NOT NULL,

                                         tax_api_state varchar(128) NOT NULL,
                                         tax_api_message varchar(128) NOT NULL,

                                         state varchar(10) NULL DEFAULT NULL,
                                         created_by INT(11) UNSIGNED NULL DEFAULT NULL,
                                         updated_by INT(11) UNSIGNED NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT factor_tax_id PRIMARY KEY (factor_tax_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `t_factor_tax`
    ADD KEY if not exists `t_factor_tax_factor_tax_id_index` (`factor_tax_id`)
