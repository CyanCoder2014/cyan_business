package com.cyancoder.bpm.repo;

import com.cyancoder.bpm.domain.ManagedObjectAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ManagedObjectAttachmentRepository extends MongoRepository<ManagedObjectAttachment, String> {
    List<ManagedObjectAttachment> findAllByObjectIdOrderByCreatedAtAsc(String objectId);
}
