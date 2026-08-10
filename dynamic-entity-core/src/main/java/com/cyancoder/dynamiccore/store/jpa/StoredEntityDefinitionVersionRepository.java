package com.cyancoder.dynamiccore.store.jpa;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface StoredEntityDefinitionVersionRepository extends JpaRepository<StoredEntityDefinitionVersion,Long>{List<StoredEntityDefinitionVersion> findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyOrderByRevisionDesc(String service,String tenant,String site,String entity);Optional<StoredEntityDefinitionVersion> findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRevision(String service,String tenant,String site,String entity,long revision);}
