package com.cyancoder.aiorchestrator.repo;
import com.cyancoder.aiorchestrator.domain.ProjectRelease;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List; import java.util.Optional;
public interface ProjectReleaseRepository extends MongoRepository<ProjectRelease,String>{List<ProjectRelease> findByDraftIdOrderByCreatedAtDesc(String draftId);Optional<ProjectRelease> findByReleaseId(String releaseId);Optional<ProjectRelease> findFirstByDraftIdAndStatusOrderByActivatedAtDesc(String draftId,String status);}
