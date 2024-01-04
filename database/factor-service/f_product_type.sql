SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_product_type` (
                                         product_type_id varchar(36) NULL DEFAULT NULL,
                                         code varchar(10) NOT NULL,
                                         name varchar(10) NOT NULL,
                                         note varchar(128) NULL DEFAULT NULL,
                                         state varchar(10) NULL DEFAULT NULL,
                                         client_id varchar(36) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT product_type_id PRIMARY KEY (product_type_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_product_type`
    ADD KEY if not exists `f_product_type_product_type_id_index` (`product_type_id`)
