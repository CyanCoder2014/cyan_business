
SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `f_factors` (
    factor_id varchar(128) NOT NULL UNIQUE,
    code varchar(10) NOT NULL,
    factor_date timestamp NULL DEFAULT NULL,
    seller_id varchar(128) NULL DEFAULT NULL,
    buyer_id varchar(128) NULL DEFAULT NULL,
    pay_state varchar(10) NULL DEFAULT NULL,
    payed DOUBLE NULL DEFAULT NULL,
    note varchar(255) NULL DEFAULT NULL,
    state varchar(10) NULL DEFAULT NULL,
    created_by INT(11) UNSIGNED NULL DEFAULT NULL,
    updated_by INT(11) UNSIGNED NULL DEFAULT NULL,
    created_at timestamp NULL DEFAULT NULL,
    updated_at timestamp NULL DEFAULT NULL,
    deleted_at timestamp NULL DEFAULT NULL,
    status  varchar(10) NULL DEFAULT NULL,

    CONSTRAINT factor_id PRIMARY KEY (factor_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `f_factors`
  ADD KEY if not exists `f_factors_seller_id_index` (`seller_id`),
  ADD KEY if not exists `f_factors_buyer_id_index` (`buyer_id`);