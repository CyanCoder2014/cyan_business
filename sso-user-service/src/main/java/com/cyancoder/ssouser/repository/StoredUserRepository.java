package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.StoredUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoredUserRepository extends JpaRepository<StoredUserEntity, String> {
    Optional<StoredUserEntity> findByEmail(String email);
}
