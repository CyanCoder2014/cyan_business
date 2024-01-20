SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `t_factor_tax` (
                                         factor_tax_id varchar(36) NOT NULL UNIQUE,
                                         factor_id varchar(128) NOT NULL,

                                         tax_api_uid varchar(128) NOT NULL,
                                         tax_api_reference varchar(128) NULL DEFAULT NULL,

                                         successed_at timestamp NULL DEFAULT NULL,
                                         tax_api_successed_uid varchar(128) NULL DEFAULT NULL,
                                         tax_api_successed_reference varchar(128) NULL DEFAULT NULL,

                                         tax_api_correction_uid varchar(128) NULL DEFAULT NULL,
                                         tax_api_correction_reference varchar(128) NULL DEFAULT NULL,

                                         tax_api_cancellation_uid varchar(128) NULL DEFAULT NULL,
                                         tax_api_cancellation_reference varchar(128) NULL DEFAULT NULL,

                                         tax_api_state varchar(128) NULL DEFAULT NULL,
                                         tax_api_message varchar(128) NULL DEFAULT NULL,

                                         tax_api_data varchar(3500) NULL DEFAULT NULL,

                                         state varchar(10) NULL DEFAULT NULL,
                                         client_id varchar(36) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT factor_tax_id PRIMARY KEY (factor_tax_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `t_factor_tax`
    ADD KEY if not exists `t_factor_tax_factor_tax_id_index` (`factor_tax_id`),
    ADD KEY if not exists `t_factor_tax_factor_id_index` (`factor_id`)
