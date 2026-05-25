package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<ClientEntity, String> {
    List<ClientEntity> findByRealmKeyOrderByClientIdAsc(String realmKey);
}
