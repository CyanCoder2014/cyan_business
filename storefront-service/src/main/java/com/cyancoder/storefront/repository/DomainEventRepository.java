package com.cyancoder.storefront.repository;
import com.cyancoder.storefront.model.DomainEventEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DomainEventRepository extends JpaRepository<DomainEventEntity,Long>{List<DomainEventEntity> findAllByDomainIdOrderByCreatedAtDesc(Long domainId);}
