package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.RealmRoleEntity;
import com.cyancoder.ssouser.entity.RealmRoleEntity.RealmRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RealmRoleRepository extends JpaRepository<RealmRoleEntity, RealmRoleId> {
    List<RealmRoleEntity> findByRealmKeyOrderByRoleKeyAsc(String realmKey);
}
