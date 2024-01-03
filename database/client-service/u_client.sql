SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `u_client` (
                                         client_id varchar(36) NOT NULL UNIQUE,
                                         name varchar(128) NOT NULL,
                                         mobile varchar(128) NULL DEFAULT NULL,
                                         email varchar(128) NULL DEFAULT NULL,
                                         note varchar(255) NULL DEFAULT NULL,
                                         expires_at timestamp NULL DEFAULT NULL,
                                         state varchar(10) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT client_id PRIMARY KEY (client_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `u_client`
    ADD KEY if not exists `u_client_client_id_index` (`client_id`)
