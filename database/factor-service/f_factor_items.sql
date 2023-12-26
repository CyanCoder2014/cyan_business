SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_factor_items` (
    id INT(11) NOT NULL AUTO_INCREMENT,
    factor_id varchar(10) NOT NULL,
    product_id timestamp NULL DEFAULT NULL,
    amount INT(11) UNSIGNED NULL DEFAULT NULL,
    unit_id INT(11) UNSIGNED NULL DEFAULT NULL,
    price  varchar(10) NULL DEFAULT NULL,
    discount DOUBLE NULL DEFAULT NULL,
    tax DOUBLE NULL DEFAULT NULL,
    other_charge DOUBLE NULL DEFAULT NULL,
    detail varchar(255) NULL DEFAULT NULL,
    state varchar(10) NULL DEFAULT NULL,
    created_by INT(11) UNSIGNED NULL DEFAULT NULL,
    updated_by INT(11) UNSIGNED NULL DEFAULT NULL,
    created_at timestamp NULL DEFAULT NULL,
    updated_at timestamp NULL DEFAULT NULL,
    deleted_at timestamp NULL DEFAULT NULL,
    status  varchar(10) NULL DEFAULT NULL,

    CONSTRAINT id PRIMARY KEY (id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_factor_items`
  ADD KEY if not exists `f_factor_items_factor_id_index` (`factor_id`),
  ADD KEY if not exists `f_factor_items_product_id_index` (`product_id`),
  ADD KEY if not exists `f_factor_items_unit_id_index` (`unit_id`)
