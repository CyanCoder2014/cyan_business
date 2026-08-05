package com.cyancoder.media.service;

import com.cyancoder.media.model.MediaByteUploadContracts.PrepareRequest;
import com.cyancoder.media.model.MediaByteUploadContracts.UploadResponse;
import com.cyancoder.media.model.MediaUploadEntity;
import com.cyancoder.media.model.MediaUploadPrepareRequest;
import com.cyancoder.media.repository.MediaUploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaByteUploadService {
    private final MediaUploadRepository repository;
    private final MediaAssetService assetService;
    private final Path storageRoot;
    private final long maxUploadBytes;
    private final long ttlSeconds;

    public MediaByteUploadService(MediaUploadRepository repository, MediaAssetService assetService,
                                  @Value("${media.storage.root}") String storageRoot,
                                  @Value("${media.storage.max-upload-bytes}") long maxUploadBytes,
                                  @Value("${media.storage.prepare-ttl-seconds}") long ttlSeconds) {
        this.repository = repository;
        this.assetService = assetService;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
        this.maxUploadBytes = maxUploadBytes;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public UploadResponse prepare(PrepareRequest request, String tenantKey, String siteKey, String actor) {
        requireTenant(tenantKey);
        if (request.sizeBytes() > maxUploadBytes) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds configured upload limit");
        MediaUploadEntity upload = new MediaUploadEntity();
        upload.setUploadId(UUID.randomUUID().toString());
        upload.setAssetKey(UUID.randomUUID().toString());
        upload.setTenantKey(tenantKey);
        upload.setSiteKey(blankToNull(siteKey));
        upload.setOriginalFileName(safeFileName(request.originalFileName()));
        upload.setMimeType(request.mimeType());
        upload.setVisibility(normalizeVisibility(request.visibility()));
        upload.setExpectedSizeBytes(request.sizeBytes());
        upload.setStatus("PREPARED");
        upload.setCreatedBy(actor);
        upload.setCreatedAt(Instant.now());
        upload.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        return response(repository.save(upload));
    }

    @Transactional
    public UploadResponse upload(String uploadId, String tenantKey, String siteKey, String actor, InputStream bytes, long contentLength) {
        MediaUploadEntity upload = scoped(uploadId, tenantKey, siteKey, actor);
        if (!"PREPARED".equals(upload.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Upload is not in PREPARED state");
        if (upload.getExpiresAt().isBefore(Instant.now())) throw new ResponseStatusException(HttpStatus.GONE, "Upload target expired");
        if (contentLength != upload.getExpectedSizeBytes()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content-Length does not match prepared size");
        Path destination = storageRoot.resolve(upload.getTenantKey()).resolve(upload.getSiteKey() == null ? "_tenant" : upload.getSiteKey()).resolve(upload.getUploadId()).normalize();
        if (!destination.startsWith(storageRoot)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload path");
        try {
            Files.createDirectories(destination.getParent());
            long copied = Files.copy(bytes, destination, StandardCopyOption.REPLACE_EXISTING);
            if (copied != upload.getExpectedSizeBytes()) {
                Files.deleteIfExists(destination);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded byte count does not match prepared size");
            }
            upload.setUploadedSizeBytes(copied);
            upload.setStoragePath(destination.toString());
            upload.setStatus("UPLOADED");
            upload.setCompletedAt(Instant.now());
            repository.save(upload);
            assetService.prepareUpload(new MediaUploadPrepareRequest(upload.getAssetKey(), mediaType(upload.getMimeType()), upload.getOriginalFileName(), upload.getMimeType(), upload.getVisibility(), upload.getOriginalFileName(), "", upload.getOriginalFileName(), "", "filesystem", destination.toString(), null, null, copied));
            return response(upload);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Media bytes could not be stored", exception);
        }
    }

    @Transactional
    public void cancel(String uploadId, String tenantKey, String siteKey, String actor) {
        MediaUploadEntity upload = scoped(uploadId, tenantKey, siteKey, actor);
        if ("UPLOADED".equals(upload.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed uploads cannot be cancelled");
        upload.setStatus("CANCELLED");
        repository.save(upload);
    }

    private MediaUploadEntity scoped(String id, String tenant, String site, String actor) {
        requireTenant(tenant);
        MediaUploadEntity upload = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found"));
        if (!upload.getTenantKey().equals(tenant) || !equalsNullable(upload.getSiteKey(), blankToNull(site)) || !upload.getCreatedBy().equals(actor)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found");
        return upload;
    }
    private UploadResponse response(MediaUploadEntity value) { return new UploadResponse(value.getUploadId(), value.getAssetKey(), "/endpoint/media/uploads/" + value.getUploadId(), "PUT", value.getStatus(), value.getExpectedSizeBytes(), value.getUploadedSizeBytes(), value.getExpiresAt(), "UPLOADED".equals(value.getStatus()) ? "/public/media/assets/" + value.getAssetKey() : null); }
    private void requireTenant(String value) { if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Key is required"); }
    private String safeFileName(String value) { String safe = Path.of(value).getFileName().toString(); if (safe.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name"); return safe; }
    private String normalizeVisibility(String value) { String result = value == null ? "PRIVATE" : value.toUpperCase(Locale.ROOT); if (!result.equals("PRIVATE") && !result.equals("PUBLIC")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "visibility must be PRIVATE or PUBLIC"); return result; }
    private String mediaType(String mime) { if (mime.startsWith("image/")) return "IMAGE"; if (mime.startsWith("video/")) return "VIDEO"; if (mime.startsWith("audio/")) return "AUDIO"; if (mime.equals("application/pdf") || mime.startsWith("text/")) return "DOCUMENT"; return "OTHER"; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private boolean equalsNullable(String left, String right) { return left == null ? right == null : left.equals(right); }
}
