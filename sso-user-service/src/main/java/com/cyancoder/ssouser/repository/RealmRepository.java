package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.RealmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealmRepository extends JpaRepository<RealmEntity, String> {
}
