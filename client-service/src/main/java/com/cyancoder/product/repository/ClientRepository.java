package com.cyancoder.product.repository;

import com.cyancoder.product.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity,String> {

    ClientEntity findByClientId(String clientId);


}
