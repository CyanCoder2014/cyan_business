SET NAMES 'utf8';
SET CHARACTER SET utf8;

CREATE TABLE if not exists `u_client_user` (
                                         client_user_id varchar(36) NOT NULL UNIQUE,
                                         client_id varchar(36) NOT NULL,
                                         user_id varchar(36) NOT NULL UNIQUE,
                                         state varchar(10) NULL DEFAULT NULL,
                                         created_by varchar(36) NULL DEFAULT NULL,
                                         updated_by varchar(36) NULL DEFAULT NULL,
                                         created_at timestamp NULL DEFAULT NULL,
                                         updated_at timestamp NULL DEFAULT NULL,
                                         deleted_at timestamp NULL DEFAULT NULL,
                                         status  varchar(10) NULL DEFAULT NULL,

                                         CONSTRAINT client_user_id PRIMARY KEY (client_user_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `u_client_user`
    ADD KEY if not exists `u_client_user_client_user_id_index` (`client_user_id`),
    ADD KEY if not exists `u_client_user_client_id_index` (`client_id`),
    ADD KEY if not exists `u_client_user_id_index` (`user_id`)
