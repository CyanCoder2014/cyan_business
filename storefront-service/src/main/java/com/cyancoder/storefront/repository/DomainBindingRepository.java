package com.cyancoder.storefront.repository;
import com.cyancoder.storefront.model.DomainBindingEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface DomainBindingRepository extends JpaRepository<DomainBindingEntity,Long>{List<DomainBindingEntity> findAllByTenantKeyAndSiteKeyOrderByCreatedAtDesc(String tenantKey,String siteKey);Optional<DomainBindingEntity> findByIdAndTenantKeyAndSiteKey(Long id,String tenantKey,String siteKey);boolean existsByDomainName(String domainName);}
