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
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import java.util.Set;

@Service
public class MediaByteUploadService {
    private final MediaUploadRepository repository;
    private final MediaAssetService assetService;
    private final Path storageRoot;
    private final long maxUploadBytes;
    private final long ttlSeconds;
    private final BillingUsageReporter usageReporter;

    public MediaByteUploadService(MediaUploadRepository repository, MediaAssetService assetService,
                                  @Value("${media.storage.root}") String storageRoot,
                                  @Value("${media.storage.max-upload-bytes}") long maxUploadBytes,
                                  @Value("${media.storage.prepare-ttl-seconds}") long ttlSeconds,
                                  BillingUsageReporter usageReporter) {
        this.repository = repository;
        this.assetService = assetService;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
        this.maxUploadBytes = maxUploadBytes;
        this.ttlSeconds = ttlSeconds;
        this.usageReporter = usageReporter;
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
            assetService.prepareUpload(new MediaUploadPrepareRequest(upload.getAssetKey(), mediaType(upload.getMimeType()), upload.getOriginalFileName(), upload.getMimeType(), upload.getVisibility(), upload.getOriginalFileName(), "", upload.getOriginalFileName(), "", "filesystem", "", null, null, copied), new DynamicScope(upload.getTenantKey(),upload.getSiteKey()));
            usageReporter.increment(upload.getTenantKey(), "storageBytes", copied);
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

    public MediaContent readInternal(String assetKey, String tenantKey, String siteKey, long maximumBytes, Set<String> allowedMimeTypes) {
        requireTenant(tenantKey);
        MediaUploadEntity upload = repository.findByAssetKey(assetKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media asset bytes not found"));
        if (!upload.getTenantKey().equals(tenantKey) || !equalsNullable(upload.getSiteKey(), blankToNull(siteKey)) || !"UPLOADED".equals(upload.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media asset bytes not found");
        }
        long limit = Math.min(maxUploadBytes, maximumBytes <= 0 ? maxUploadBytes : maximumBytes);
        if (upload.getUploadedSizeBytes() > limit) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Media asset exceeds consumer byte limit");
        if (allowedMimeTypes != null && !allowedMimeTypes.isEmpty() && allowedMimeTypes.stream().noneMatch(pattern -> mimeMatches(upload.getMimeType(), pattern))) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Media asset type is not allowed");
        }
        try {
            Path path = Path.of(upload.getStoragePath()).toAbsolutePath().normalize();
            if (!path.startsWith(storageRoot) || !Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media asset bytes not found");
            return new MediaContent(upload.getAssetKey(), upload.getOriginalFileName(), upload.getMimeType(), upload.getUploadedSizeBytes(), Files.readAllBytes(path));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Media bytes could not be read", exception);
        }
    }

    private boolean mimeMatches(String mime, String pattern) { return pattern.endsWith("/*") ? mime.startsWith(pattern.substring(0, pattern.length() - 1)) : mime.equalsIgnoreCase(pattern); }
    public record MediaContent(String assetKey, String fileName, String mimeType, long sizeBytes, byte[] bytes) {}

    @Transactional
    public UploadResponse ingestGenerated(com.cyancoder.media.model.MediaByteUploadContracts.GeneratedAssetRequest request,String tenantKey,String siteKey) {
        requireTenant(tenantKey);
        byte[] bytes;
        try { bytes=java.util.Base64.getDecoder().decode(request.base64()); }
        catch(IllegalArgumentException exception){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Generated asset base64 is invalid");}
        if(bytes.length==0||bytes.length>maxUploadBytes)throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Generated asset exceeds configured media limit");
        if(!(request.mimeType().startsWith("image/")||request.mimeType().startsWith("audio/")||request.mimeType().startsWith("video/")||request.mimeType().startsWith("text/")||request.mimeType().equals("application/json")))throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Generated artifact media type is not supported");
        MediaUploadEntity upload=new MediaUploadEntity();Instant now=Instant.now();upload.setUploadId(UUID.randomUUID().toString());upload.setAssetKey(UUID.randomUUID().toString());upload.setTenantKey(tenantKey);upload.setSiteKey(blankToNull(siteKey));upload.setOriginalFileName(safeFileName(request.fileName()));upload.setMimeType(request.mimeType());upload.setVisibility("PRIVATE");upload.setExpectedSizeBytes(bytes.length);upload.setUploadedSizeBytes(bytes.length);upload.setStatus("UPLOADED");upload.setCreatedBy(request.generatedBy());upload.setCreatedAt(now);upload.setCompletedAt(now);upload.setExpiresAt(now.plusSeconds(Math.max(1,Math.min(request.retentionDays()==null?30:request.retentionDays(),365))*86400L));Path destination=storageRoot.resolve(tenantKey).resolve(upload.getSiteKey()==null?"_tenant":upload.getSiteKey()).resolve(upload.getUploadId()).normalize();if(!destination.startsWith(storageRoot))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid generated asset path");try{Files.createDirectories(destination.getParent());Files.write(destination,bytes);upload.setStoragePath(destination.toString());repository.save(upload);assetService.prepareUpload(new MediaUploadPrepareRequest(upload.getAssetKey(),mediaType(upload.getMimeType()),upload.getOriginalFileName(),upload.getMimeType(),"PRIVATE",upload.getOriginalFileName(),"",upload.getOriginalFileName(),"","filesystem","",null,null,(long)bytes.length),new DynamicScope(tenantKey,upload.getSiteKey()));usageReporter.increment(tenantKey,"storageBytes",bytes.length);return response(upload);}catch(IOException exception){throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Generated media could not be stored",exception);}
    }

    private MediaUploadEntity scoped(String id, String tenant, String site, String actor) {
        requireTenant(tenant);
        MediaUploadEntity upload = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found"));
        if (!upload.getTenantKey().equals(tenant) || !equalsNullable(upload.getSiteKey(), blankToNull(site)) || !upload.getCreatedBy().equals(actor)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found");
        return upload;
    }
    private UploadResponse response(MediaUploadEntity value) { return new UploadResponse(value.getUploadId(), value.getAssetKey(), "/endpoint/media/uploads/" + value.getUploadId(), "PUT", value.getStatus(), value.getExpectedSizeBytes(), value.getUploadedSizeBytes(), value.getExpiresAt(), "UPLOADED".equals(value.getStatus()) ? ("PUBLIC".equals(value.getVisibility())?"/public/media/content/":"/endpoint/media/assets/") + value.getAssetKey() + ("PUBLIC".equals(value.getVisibility())?"":"/content") : null); }
    private void requireTenant(String value) { if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Key is required"); }
    private String safeFileName(String value) { String safe = Path.of(value).getFileName().toString(); if (safe.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name"); return safe; }
    private String normalizeVisibility(String value) { String result = value == null ? "PRIVATE" : value.toUpperCase(Locale.ROOT); if (!result.equals("PRIVATE") && !result.equals("PUBLIC")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "visibility must be PRIVATE or PUBLIC"); return result; }
    private String mediaType(String mime) { if (mime.startsWith("image/")) return "IMAGE"; if (mime.startsWith("video/")) return "VIDEO"; if (mime.startsWith("audio/")) return "AUDIO"; if (mime.equals("application/pdf") || mime.startsWith("text/")) return "DOCUMENT"; return "OTHER"; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
    private boolean equalsNullable(String left, String right) { return left == null ? right == null : left.equals(right); }
}
