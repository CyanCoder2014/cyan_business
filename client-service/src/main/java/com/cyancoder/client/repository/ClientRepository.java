package com.cyancoder.client.repository;

import com.cyancoder.client.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity,String> {

    ClientEntity findByClientId(String clientId);


}
