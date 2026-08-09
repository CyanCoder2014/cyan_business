package com.cyancoder.media.repository;
import com.cyancoder.media.model.MediaUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface MediaUploadRepository extends JpaRepository<MediaUploadEntity, String> {
 List<MediaUploadEntity> findAllByTenantKeyAndSiteKeyOrderByCreatedAtDesc(String tenantKey,String siteKey);
 Optional<MediaUploadEntity> findByAssetKey(String assetKey);
 Optional<MediaUploadEntity> findByAssetKeyAndTenantKeyAndSiteKey(String assetKey,String tenantKey,String siteKey);
}
