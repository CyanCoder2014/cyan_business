package com.cyancoder.ssoauth.repository;

import com.cyancoder.ssoauth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
    List<RefreshTokenEntity> findBySessionId(String sessionId);
}
