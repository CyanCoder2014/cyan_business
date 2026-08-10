package com.cyancoder.media.service;

import com.cyancoder.media.model.MediaByteUploadContracts.PrepareRequest;
import com.cyancoder.media.model.MediaUploadEntity;
import com.cyancoder.media.repository.MediaUploadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaByteUploadServiceTest {
    @TempDir Path root;
    @Test void storesPreparedBytesAndFinalizesRealAssetMetadata() {
        MediaUploadRepository repository=mock(MediaUploadRepository.class);
        MediaAssetService assets=mock(MediaAssetService.class);
        when(repository.save(any())).thenAnswer(invocation->invocation.getArgument(0));
        var service=new MediaByteUploadService(repository,assets,root.toString(),1024,900);
        var prepared=service.prepare(new PrepareRequest("brief.txt","text/plain",4,"PRIVATE"),"tenant","site","actor");
        MediaUploadEntity stored=new MediaUploadEntity();stored.setUploadId(prepared.uploadId());stored.setAssetKey(prepared.assetKey());stored.setTenantKey("tenant");stored.setSiteKey("site");stored.setOriginalFileName("brief.txt");stored.setMimeType("text/plain");stored.setVisibility("PRIVATE");stored.setExpectedSizeBytes(4);stored.setStatus("PREPARED");stored.setCreatedBy("actor");stored.setExpiresAt(java.time.Instant.now().plusSeconds(60));
        when(repository.findById(prepared.uploadId())).thenReturn(Optional.of(stored));
        var completed=service.upload(prepared.uploadId(),"tenant","site","actor",new ByteArrayInputStream("test".getBytes()),4);
        assertThat(completed.status()).isEqualTo("UPLOADED");
        assertThat(completed.uploadedSizeBytes()).isEqualTo(4);
        verify(assets).prepareUpload(any(), any());
    }
}
