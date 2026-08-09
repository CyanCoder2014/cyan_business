package com.cyancoder.media.model;
import java.time.Instant;import java.util.List;import java.util.Map;
public final class MediaAssetContracts{private MediaAssetContracts(){}public record AssetItem(String assetKey,String originalFileName,String mimeType,String assetType,String visibility,long sizeBytes,String status,String deliveryUrl,Instant createdAt,Instant completedAt,Map<String,Object> metadata){}public record AssetPage(List<AssetItem>items,int page,int size,long total){}public record UpdateAssetRequest(String title,String altText,String caption,String license,List<String>tags,String folderKey,String visibility){}public record UsageResponse(String assetKey,String status,long referenceCount,List<Map<String,Object>>references,String reason){}
}
