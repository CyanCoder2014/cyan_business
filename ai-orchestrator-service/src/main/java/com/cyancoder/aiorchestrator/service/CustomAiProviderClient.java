package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.AiOperationRequest;
import com.cyancoder.aiorchestrator.domain.AiProviderProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomAiProviderClient {
    private final ObjectMapper json; private final RestTemplate http;
    public CustomAiProviderClient(ObjectMapper json){this.json=json;SimpleClientHttpRequestFactory f=new SimpleClientHttpRequestFactory();f.setConnectTimeout(5000);f.setReadTimeout(120000);http=new RestTemplate(f);}
    public ProviderResult execute(AiProviderProfile profile,String secret,AiOperationRequest request,List<AiMediaAssetClient.ResolvedAsset> assets){HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);headers.setBearerAuth(secret);Map<String,Object> body=new LinkedHashMap<>();body.put("model",profile.getModel());body.put("operation",request.operation().name());body.put("instructions",request.instructions());if(request.input()!=null)body.put("input",request.input());if(request.outputSchema()!=null&&!request.outputSchema().isEmpty())body.put("outputSchema",request.outputSchema());if(request.locale()!=null&&!request.locale().isBlank())body.put("locale",request.locale());body.put("assets",assets);ResponseEntity<String> response=http.exchange(profile.getBaseUrl()+profile.getOperationPath(),HttpMethod.POST,new HttpEntity<>(body,headers),String.class);try{JsonNode root=json.readTree(response.getBody());JsonNode output=root.has("output")?root.get("output"):root.path("choices").path(0).path("message").path("content");Object value=output.isTextual()?output.asText():json.treeToValue(output,Object.class);Map<String,Object> usage=root.has("usage")?json.convertValue(root.get("usage"),Map.class):Map.of();return new ProviderResult(value,usage);}catch(Exception e){throw new IllegalStateException("Custom AI provider returned an invalid response",e);}}
    public record ProviderResult(Object output,Map<String,Object> usage){}
}
