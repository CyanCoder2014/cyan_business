package com.cyancoder.media.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import com.cyancoder.dynamiccore.runtime.DynamicRecordRequest;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.media.model.MediaAssetResponse;
import com.cyancoder.media.model.MediaUploadPrepareRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MediaAssetService {
    private final DynamicRuntimeService dynamicRuntimeService;

    public MediaAssetService(DynamicRuntimeService dynamicRuntimeService) {
        this.dynamicRuntimeService = dynamicRuntimeService;
    }

    public MediaAssetResponse prepareUpload(MediaUploadPrepareRequest request, DynamicScope scope) {
        ensureDefinition("media-asset", scope);
        String assetKey = request.assetKey();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assetKey", assetKey);
        data.put("assetType", firstNonBlank(request.assetType(), "IMAGE"));
        data.put("originalFileName", request.originalFileName());
        data.put("mimeType", request.mimeType());
        data.put("visibility", firstNonBlank(request.visibility(), "PUBLIC"));
        data.put("seo", Map.of(
                "altText", firstNonBlank(request.altText(), request.title(), assetKey),
                "caption", firstNonBlank(request.caption(), ""),
                "title", firstNonBlank(request.title(), request.originalFileName()),
                "license", firstNonBlank(request.license(), "")
        ));
        String path = "assets/" + assetKey + "/" + request.originalFileName();
        String cdnUrl = "/public/media/content/" + assetKey;
        data.put("storage", Map.of(
                "bucket", firstNonBlank(request.bucket(), "default-public"),
                "path", path,
                "cdnUrl", cdnUrl,
                "width", request.width() == null ? 0 : request.width(),
                "height", request.height() == null ? 0 : request.height(),
                "sizeBytes", request.sizeBytes() == null ? 0 : request.sizeBytes()
        ));
        data.put("variants", List.of());
        data.put("storageStatus", "UPLOADED");

        data.put("tags", List.of());
        data.put("folderKey", "");
        DynamicEntityRecordDocument saved = dynamicRuntimeService.submitMap("media-asset", assetKey, data, true, scope);
        return toResponse(saved, null);
    }

    public MediaAssetResponse get(String assetKey) {
        return toResponse(dynamicRuntimeService.getRecord("media-asset", assetKey), null);
    }

    public MediaAssetResponse get(String assetKey, DynamicScope scope) { return toResponse(dynamicRuntimeService.getRecord("media-asset",assetKey,scope),null); }
    public Map<String,Object> update(String assetKey, Map<String,Object> changes, DynamicScope scope){DynamicRecordRequest r=new DynamicRecordRequest();r.setData(changes);return dynamicRuntimeService.update("media-asset",assetKey,r,false,scope).getData();}

    @SuppressWarnings("unchecked")
    public MediaAssetResponse getVariant(String assetKey, String variantKey) {
        DynamicEntityRecordDocument record = dynamicRuntimeService.getRecord("media-asset", assetKey);
        Map<String, Object> data = record.getData() == null ? Map.of() : record.getData();
        Object variants = data.get("variants");
        if (!(variants instanceof List<?> list)) {
            return toResponse(record, null);
        }
        Map<String, Object> variant = list.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(item -> (Map<String, Object>) item)
                .filter(item -> variantKey.equals(Objects.toString(item.get("variantKey"), "")))
                .findFirst()
                .orElseThrow();
        return toResponse(record, variant);
    }

    private MediaAssetResponse toResponse(DynamicEntityRecordDocument record, Map<String, Object> variant) {
        Map<String, Object> data = record.getData() == null ? Map.of() : record.getData();
        String deliveryUrl = variant == null
                ? Objects.toString(nested(data, "storage", "cdnUrl"), "")
                : Objects.toString(variant.get("cdnUrl"), "");
        return new MediaAssetResponse(record.getRecordKey(), deliveryUrl, Objects.toString(data.get("storageStatus"), "UPLOADED"), data);
    }

    private void ensureDefinition(String entityKey, DynamicScope scope) {
        try {
            dynamicRuntimeService.getDefinition(entityKey, scope);
        } catch (Exception ex) {
            dynamicRuntimeService.createFromTemplate(entityKey, entityKey, scope);
        }
    }

    @SuppressWarnings("unchecked")
    private Object nested(Map<String, Object> data, String objectKey, String fieldKey) {
        Object nested = data.get(objectKey);
        if (nested instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).get(fieldKey);
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
