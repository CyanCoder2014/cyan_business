package com.cyancoder.bpm.repo;

import com.cyancoder.bpm.domain.ManagedObjectComment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ManagedObjectCommentRepository extends MongoRepository<ManagedObjectComment, String> {
    List<ManagedObjectComment> findAllByObjectIdOrderByCreatedAtAsc(String objectId);
}
