package com.cyancoder.aiorchestrator.repo;
import com.cyancoder.aiorchestrator.domain.AiArtifactJob;import java.util.List;import java.util.Optional;import org.springframework.data.mongodb.repository.MongoRepository;
public interface AiArtifactJobRepository extends MongoRepository<AiArtifactJob,String>{Optional<AiArtifactJob> findByJobIdAndTenantKeyAndSiteKey(String id,String tenant,String site);List<AiArtifactJob> findTop50ByTenantKeyAndSiteKeyOrderByCreatedAtDesc(String tenant,String site);}
