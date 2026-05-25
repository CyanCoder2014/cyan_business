package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.ClientRoleEntity;
import com.cyancoder.ssouser.entity.ClientRoleEntity.ClientRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRoleRepository extends JpaRepository<ClientRoleEntity, ClientRoleId> {
    List<ClientRoleEntity> findByClientIdOrderByRoleKeyAsc(String clientId);
}
