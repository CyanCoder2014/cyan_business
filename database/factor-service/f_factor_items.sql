SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_factor_items` (
    factor_item_id varchar(36) NOT NULL UNIQUE,
    factor_id varchar(36) NOT NULL,
    product_id varchar(36) NOT NULL,
    amount DOUBLE NOT NULL,
    price  DOUBLE NOT NULL,
    discount DOUBLE NULL DEFAULT NULL,
    tax DOUBLE NULL DEFAULT NULL,
    other_charge DOUBLE NULL DEFAULT NULL,
    detail varchar(255) NULL DEFAULT NULL,
    state varchar(10) NULL DEFAULT NULL,
    client_id varchar(36) NULL DEFAULT NULL,
    created_by varchar(36) NULL DEFAULT NULL,
    updated_by varchar(36) NULL DEFAULT NULL,
    created_at timestamp NULL DEFAULT NULL,
    updated_at timestamp NULL DEFAULT NULL,
    deleted_at timestamp NULL DEFAULT NULL,
    status  varchar(10) NULL DEFAULT NULL,

    CONSTRAINT factor_item_id PRIMARY KEY (factor_item_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_factor_items`
  ADD KEY if not exists `f_factor_items_factor_id_index` (`factor_id`),
  ADD KEY if not exists `f_factor_items_product_id_index` (`product_id`)