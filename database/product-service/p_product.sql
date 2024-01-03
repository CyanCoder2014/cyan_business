SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `p_product` (
                                         product_id varchar(36) NOT NULL UNIQUE,
                                         unit_id varchar(36) NOT NULL,
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

                                         CONSTRAINT product_id PRIMARY KEY (product_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `p_product`
    ADD KEY if not exists `p_product_product_id_index` (`product_id`),
    ADD KEY if not exists `p_product_unit_id_index` (`unit_id`)
