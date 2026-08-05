package com.cyancoder.media.repository;
import com.cyancoder.media.model.MediaUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MediaUploadRepository extends JpaRepository<MediaUploadEntity, String> {}
