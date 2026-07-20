package com.cyancoder.automationorchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gorules.zen_engine.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GoRulesDecisionService {
    private final ObjectMapper mapper;
    private final ResourceLoader resources;
    public GoRulesDecisionService(ObjectMapper mapper, ResourceLoader resources) { this.mapper=mapper; this.resources=resources; }
    public Map<String,Object> evaluate(Map<String,Object> config,Map<String,Object> input){
        try(ZenEngine engine=new ZenEngine(null,null);ZenDecision decision=engine.createDecision(new JsonBuffer(bytes(config)))){
            ZenEngineResponse response=decision.evaluate(new JsonBuffer(mapper.writeValueAsBytes(input)),new ZenEvaluateOptions(null,AutomationDataSupport.bool(config.get("trace"),false))).join();
            Map<String,Object> out=new LinkedHashMap<>();out.put("result",mapper.readValue(response.result().value(),Object.class));
            if(response.performance()!=null)out.put("performance",response.performance());
            if(response.trace()!=null&&!response.trace().isEmpty())out.put("trace",response.trace().keySet());
            return out;
        }catch(Exception ex){throw new IllegalStateException("GoRules decision evaluation failed",ex);}
    }
    private byte[] bytes(Map<String,Object> config)throws Exception{
        Object jdm=config.get("jdm");if(jdm!=null)return jdm instanceof String s?s.getBytes(StandardCharsets.UTF_8):mapper.writeValueAsBytes(jdm);
        String resource=AutomationDataSupport.string(config.get("classpathResource"));if(resource!=null)return resources.getResource(resource.startsWith("classpath:")?resource:"classpath:"+resource).getInputStream().readAllBytes();
        String file=AutomationDataSupport.string(config.get("filePath"));if(file!=null)return Files.readAllBytes(Path.of(file));
        throw new IllegalArgumentException("one of jdm, classpathResource, or filePath is required");
    }
}
