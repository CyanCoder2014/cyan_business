package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.AiOperationRequest.AiAssetInput;
import com.cyancoder.aiorchestrator.client.impl.InternalServiceHttpSupport;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AiMediaAssetClient {
    private static final long MAX_TOTAL_BYTES=50L*1024*1024;
    private final InternalServiceHttpSupport http;
    public AiMediaAssetClient(InternalServiceHttpSupport http){this.http=http;}
    public List<ResolvedAsset> resolve(String tenant,String site,List<AiAssetInput> inputs,Set<String> providerModalities){List<ResolvedAsset> result=new ArrayList<>();long total=0;for(AiAssetInput input:inputs==null?List.<AiAssetInput>of():inputs){String modality=input.modality().trim().toUpperCase(Locale.ROOT);if(!Set.of("IMAGE","AUDIO","VIDEO","FILE").contains(modality))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported asset modality");if(!providerModalities.contains(modality))throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Provider profile does not support "+modality);String mimePattern=switch(modality){case"IMAGE"->"image/*";case"AUDIO"->"audio/*";case"VIDEO"->"video/*";default->"application/*";};var binary=http.getBytes("media-service","/internal/media/assets/"+encode(input.assetKey())+"/content?maxBytes="+MAX_TOTAL_BYTES+"&allowedMimeType="+encode(mimePattern),tenant,site);total+=binary.bytes().length;if(total>MAX_TOTAL_BYTES)throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Combined AI assets exceed 50 MiB");result.add(new ResolvedAsset(input.assetKey(),modality,binary.mimeType(),binary.fileName(),Base64.getEncoder().encodeToString(binary.bytes())));}return result;}
    private String encode(String v){return URLEncoder.encode(v, StandardCharsets.UTF_8);}public record ResolvedAsset(String assetKey,String modality,String mimeType,String fileName,String base64){}
}
