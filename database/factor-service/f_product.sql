SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_products` (
                                         product_id varchar(36) NOT NULL UNIQUE,
                                         product_type_id varchar(36) NULL DEFAULT NULL,
                                         unit_id varchar(36) NOT NULL,
                                         code varchar(128) NOT NULL,
                                         name varchar(128) NOT NULL,
                                         note varchar(128) NULL DEFAULT NULL,
                                         state varchar(10) NULL DEFAULT NULL,
                                         client_id varchar(36) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT product_id PRIMARY KEY (product_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_products`
    ADD KEY if not exists `f_products_product_id_index` (`product_id`),
    ADD KEY if not exists `f_products_unit_id_index` (`unit_id`),
    ADD KEY if not exists `f_products_product_type_id_index` (`product_type_id`)
