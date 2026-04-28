package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.StoredUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredUserRepository extends JpaRepository<StoredUserEntity, String> {
}
