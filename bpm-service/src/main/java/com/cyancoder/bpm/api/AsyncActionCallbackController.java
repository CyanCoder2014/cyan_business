package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.AsyncActionCallbackRequest;
import com.cyancoder.bpm.api.dto.FlowScopeResolver;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.config.DynamicFlowCallbackProperties;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.service.AsyncCallbackSecurityService;
import com.cyancoder.bpm.service.ObjectFlowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.util.Set;

@RestController
@RequestMapping("/public/bpm/async-actions/callbacks")
public class AsyncActionCallbackController {
    private final ObjectFlowService objectFlowService;
    private final AsyncCallbackSecurityService callbackSecurityService;
    private final DynamicFlowCallbackProperties callbackProperties;
    private final ObjectMapper objectMapper;

    public AsyncActionCallbackController(ObjectFlowService objectFlowService,
                                         AsyncCallbackSecurityService callbackSecurityService,
                                         DynamicFlowCallbackProperties callbackProperties,
                                         ObjectMapper objectMapper) {
        this.objectFlowService = objectFlowService;
        this.callbackSecurityService = callbackSecurityService;
        this.callbackProperties = callbackProperties;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/{correlationKey}")
    public ManagedObject callbackByCorrelationKey(@PathVariable String correlationKey,
                                                  @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
                                                  @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
                                                  HttpServletRequest httpServletRequest,
                                                  @RequestBody JsonNode requestBody) throws Exception {
        byte[] bodyBytes = objectMapper.writeValueAsBytes(requestBody);
        String timestamp = httpServletRequest.getHeader(callbackProperties.getTimestampHeader());
        String signature = httpServletRequest.getHeader(callbackProperties.getSignatureHeader());
        callbackSecurityService.validate(timestamp, signature, bodyBytes);
        AsyncActionCallbackRequest request = objectMapper.treeToValue(requestBody, AsyncActionCallbackRequest.class);
        return objectFlowService.acceptAsyncActionCallbackByCorrelationKey(
                FlowScopeResolver.fromHeaders(tenantKey, siteKey),
                correlationKey,
                request,
                new TransitionActorContext("system-callback", Set.of("creator"), Set.of("FLOW_SUBMIT")),
                callbackFingerprint(request, signature, bodyBytes)
        );
    }

    private String callbackFingerprint(AsyncActionCallbackRequest request, String signature, byte[] bodyBytes) throws Exception {
        if (request != null && request.callbackId() != null && !request.callbackId().isBlank()) {
            return "callbackId:" + request.callbackId().trim();
        }
        if (signature != null && !signature.isBlank()) {
            return "signature:" + signature;
        }
        return "body:" + java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bodyBytes));
    }
}
