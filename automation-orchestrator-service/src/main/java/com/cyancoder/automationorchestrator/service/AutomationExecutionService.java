package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.config.AutomationCallbackProperties;
import com.cyancoder.automationorchestrator.config.AutomationWorkerProperties;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionMode;
import com.cyancoder.automationorchestrator.domain.AutomationFailurePolicy;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.model.BpmAsyncCallbackRequest;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.dao.DuplicateKeyException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AutomationExecutionService {
    private static final String MONGO_DOT_ESCAPE = "\uFF0E";
    private final AutomationExecutionRepository repository;
    private final InternalServiceHttpSupport httpSupport;
    private final AutomationCallbackProperties callbackProperties;
    private final ObjectMapper objectMapper;
    private final PipelineAutomationRuntime pipelineRuntime;
    private final AutomationFlowDefinitionService flowDefinitionService;
    private final GraphAutomationRuntime graphRuntime;
    private final ItemStreamAutomationRuntime itemStreamRuntime;
    private final AutomationWorkerProperties workerProperties;
    private final BillingUsageReporter usageReporter;

    public AutomationExecutionService(AutomationExecutionRepository repository,
                                      InternalServiceHttpSupport httpSupport,
                                      AutomationCallbackProperties callbackProperties,
                                      ObjectMapper objectMapper) {
        this(repository, httpSupport, callbackProperties, objectMapper, null, null, null, new AutomationWorkerProperties(), new BillingUsageReporter(httpSupport));
    }

    @Autowired
    public AutomationExecutionService(AutomationExecutionRepository repository,
                                      InternalServiceHttpSupport httpSupport,
                                      AutomationCallbackProperties callbackProperties,
                                      ObjectMapper objectMapper,
                                      AutomationFlowDefinitionService flowDefinitionService,
                                      GraphAutomationRuntime graphRuntime,
                                      ItemStreamAutomationRuntime itemStreamRuntime,
                                      AutomationWorkerProperties workerProperties,
                                      BillingUsageReporter usageReporter) {
        this.repository = repository;
        this.httpSupport = httpSupport;
        this.callbackProperties = callbackProperties;
        this.objectMapper = objectMapper;
        this.pipelineRuntime = new PipelineAutomationRuntime(httpSupport);
        this.flowDefinitionService = flowDefinitionService;
        this.graphRuntime = graphRuntime;
        this.itemStreamRuntime = itemStreamRuntime;
        this.workerProperties = workerProperties;
        this.usageReporter = usageReporter;
    }

    public AutomationStartResponse start(AutomationStartRequest request) {
        String tenantKey = scope(request.tenantKey());
        String siteKey = scope(request.siteKey());
        String idempotencyKey = normalize(request.idempotencyKey());
        if (idempotencyKey != null) {
            Optional<AutomationExecution> existing = repository
                    .findFirstByTenantKeyAndSiteKeyAndIdempotencyKeyOrderByCreatedAtDesc(tenantKey, siteKey, idempotencyKey);
            if (existing != null && existing.isPresent() && !isTerminalFailure(existing.get().getStatus())) {
                return toResponse(existing.get());
            }
        }

        AutomationExecution execution = new AutomationExecution();
        execution.setExecutionId("exec-" + UUID.randomUUID());
        execution.setBlockKey(firstNonBlank(request.blockKey(), request.flowKey(), request.automationFlowKey(), "block-" + UUID.randomUUID().toString().substring(0, 8)));
        execution.setAutomationFlowKey(firstNonBlank(request.automationFlowKey(), request.flowKey(), "hybrid-screening-automation"));
        execution.setManagedObjectId(request.managedObjectId());
        execution.setIdempotencyKey(idempotencyKey);
        execution.setExecutionMode(request.executionMode() == null ? AutomationExecutionMode.SYNC : request.executionMode());
        execution.setFailurePolicy(request.failurePolicy() == null ? AutomationFailurePolicy.FAIL_FAST : request.failurePolicy());
        execution.setCorrelationKey(firstNonBlank(request.correlationKey(), "corr-" + UUID.randomUUID()));
        execution.setInitiatedBy(Objects.toString(request.context() == null ? null : request.context().get("initiatedBy"), null));
        execution.setAuthorizationMode(Objects.toString(request.context() == null ? null : request.context().get("authorizationMode"), "SERVICE"));
        execution.setTenantKey(tenantKey);
        execution.setSiteKey(siteKey);
        execution.setStatus("RUNNING");
        execution.setInput(new LinkedHashMap<>(firstMap(request.input(), request.variables())));
        execution.setOutput(new LinkedHashMap<>(execution.getInput()));
        execution.setContext(request.context());
        execution.setCallbackPath(request.callbackPath());
        Map<String, Object> requestedInline = firstMap(request.inlineFragment(), request.inlineFlow());
        AutomationFlowDefinition graph = resolveRequestedGraph(request, requestedInline);
        if (graph != null) {
            execution.setFlowVersion(graph.getVersion());
            execution.setEntryType(requestedInline.isEmpty() ? "SAVED_FLOW" : "INLINE_FLOW");
            execution.setInlineFragment(mongoSafeMap(objectMapper.convertValue(graph, Map.class)));
            execution.setCurrentNodeId(graph.getEntryNodeId());
        } else {
            execution.setInlineFragment(mongoSafeMap(requestedInline));
        }
        execution.setMaxRetries(request.maxRetries() == null ? 0 : Math.max(0, request.maxRetries()));
        execution.setTimeoutSeconds(request.timeoutSeconds());
        execution.setTimeoutAt(request.timeoutSeconds() == null ? null : Instant.now().plusSeconds(request.timeoutSeconds()));
        execution.setCreatedAt(Instant.now());
        execution.setUpdatedAt(Instant.now());
        claimForLocalWorker(execution);
        try {
            repository.save(execution);
        } catch (DuplicateKeyException duplicate) {
            if (idempotencyKey == null) throw duplicate;
            Optional<AutomationExecution> concurrent = repository
                    .findFirstByTenantKeyAndSiteKeyAndIdempotencyKeyOrderByCreatedAtDesc(tenantKey, siteKey, idempotencyKey);
            if (concurrent.isPresent()) return toResponse(concurrent.get());
            throw duplicate;
        }
        usageReporter.increment(tenantKey, "automationRuns");

        if (execution.getExecutionMode() == AutomationExecutionMode.SYNC) {
            executeNow(execution);
            return toResponse(repository.findByExecutionId(execution.getExecutionId()).orElse(execution));
        }

        long delayMillis = request.delayMillis() == null ? 0L : Math.max(0L, request.delayMillis());
        CompletableFuture.runAsync(() -> {
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            executeNow(execution);
        });
        return toResponse(execution);
    }

    public AutomationStartResponse startAuthorized(AutomationStartRequest request, Set<String> roles) {
        return startAuthorized(request, roles, request.tenantKey(), request.siteKey());
    }

    public AutomationStartResponse startAuthorized(AutomationStartRequest request, Set<String> roles, String tenantKey, String siteKey) {
        return startAuthorized(request, roles, tenantKey, siteKey, null);
    }

    public AutomationStartResponse startAuthorized(AutomationStartRequest request, Set<String> roles, String tenantKey, String siteKey, String actor) {
        request = withScope(request, tenantKey, siteKey);
        Map<String,Object> inline = firstMap(request.inlineFragment(), request.inlineFlow());
        AutomationFlowDefinition definition = resolveRequestedGraph(request, inline);
        if (definition != null) flowDefinitionService.requireRoles(definition, roles);
        Map<String,Object> context = new LinkedHashMap<>(request.context() == null ? Map.of() : request.context());
        context.put("actorRoles", roles == null ? List.of() : roles);
        context.put("initiatedBy", actor == null || actor.isBlank() ? "authenticated-user" : actor);
        context.put("authorizationMode", "USER_EFFECTIVE_ACCESS");
        return start(new AutomationStartRequest(request.blockKey(), request.automationFlowKey(), request.executionMode(), request.failurePolicy(),
                request.correlationKey(), request.callbackPath(), request.tenantKey(), request.siteKey(), request.input(), context,
                request.inlineFragment(), request.maxRetries(), request.timeoutSeconds(), request.delayMillis(), request.flowKey(),
                request.managedObjectId(), request.idempotencyKey(), request.variables(), request.inlineFlow()));
    }

    public boolean requiresAi(AutomationStartRequest request, String tenantKey, String siteKey) {
        request = withScope(request, tenantKey, siteKey);
        AutomationFlowDefinition definition = resolveRequestedGraph(request, firstMap(request.inlineFragment(), request.inlineFlow()));
        return definition != null && definition.getNodes().stream().anyMatch(node -> node.type() == com.cyancoder.automationorchestrator.domain.AutomationNodeType.AI_OPERATION);
    }

    public boolean requiresAiFlow(String tenantKey, String siteKey, String flowKey, Integer version, Map<String,Object> request) {
        AutomationFlowDefinition definition = version == null
                ? flowDefinitionService.active(tenantKey, siteKey, flowKey, Objects.toString(request.getOrDefault("environment", "default")))
                : flowDefinitionService.get(tenantKey, siteKey, flowKey, version);
        return definition.getNodes().stream().anyMatch(node -> node.type() == com.cyancoder.automationorchestrator.domain.AutomationNodeType.AI_OPERATION);
    }

    public AutomationStartResponse get(String executionId) {
        return toResponse(repository.findByExecutionId(executionId).orElseThrow());
    }

    public AutomationStartResponse get(String executionId, String tenantKey, String siteKey) {
        return toResponse(scopedExecution(executionId, tenantKey, siteKey));
    }

    public AutomationStartResponse triggerWebhook(String flowKey, String tenantKey, String siteKey,
                                                   Map<String, Object> payload, Map<String, Object> context,
                                                   String idempotencyKey) {
        return start(new AutomationStartRequest(
                flowKey, flowKey, AutomationExecutionMode.ASYNC, AutomationFailurePolicy.MARK_FAILED,
                null, null, tenantKey, siteKey, payload, context, null, 0, null, 0L,
                flowKey, null, idempotencyKey, payload, null
        ));
    }

    public AutomationStartResponse manualRun(String tenantKey, String siteKey, String flowKey, Integer version,
                                              Map<String, Object> request, Set<String> roles) {
        return manualRun(tenantKey, siteKey, flowKey, version, request, roles, null);
    }

    public AutomationStartResponse manualRun(String tenantKey, String siteKey, String flowKey, Integer version,
                                              Map<String, Object> request, Set<String> roles, String actor) {
        AutomationFlowDefinition definition = version == null
                ? flowDefinitionService.active(tenantKey, siteKey, flowKey, Objects.toString(request.getOrDefault("environment", "default")))
                : flowDefinitionService.get(tenantKey, siteKey, flowKey, version);
        flowDefinitionService.requireRoles(definition, roles);
        Map<String, Object> context = new LinkedHashMap<>(firstMap(AutomationDataSupport.map(request.get("context")), Map.of()));
        context.put("runMode", "MANUAL");
        context.put("actorRoles", roles == null ? List.of() : roles);
        context.put("initiatedBy", actor == null || actor.isBlank() ? "authenticated-user" : actor);
        context.put("authorizationMode", "USER_EFFECTIVE_ACCESS");
        if (request.get("startNodeId") != null) context.put("startNodeId", request.get("startNodeId"));
        Map<String, Object> input = new LinkedHashMap<>(AutomationDataSupport.map(request.get("input")));
        if (request.get("items") != null) input.put("items", request.get("items"));
        Map<String, Object> inline = objectMapper.convertValue(definition, Map.class);
        AutomationStartRequest start = new AutomationStartRequest(
                flowKey, flowKey,
                Boolean.FALSE.equals(request.get("async")) ? AutomationExecutionMode.SYNC : AutomationExecutionMode.ASYNC,
                AutomationFailurePolicy.MARK_FAILED, null, null, tenantKey, siteKey, input, context, inline,
                0, null, 0L, flowKey, null, null, input, inline
        );
        return start(start);
    }

    public List<AutomationStartResponse> history(String tenantKey, String siteKey, String flowKey, String status) {
        return repository.findAllByTenantKeyAndSiteKeyOrderByCreatedAtDesc(scope(tenantKey), scope(siteKey)).stream()
                .filter(execution -> flowKey == null || flowKey.isBlank() || flowKey.equals(execution.getAutomationFlowKey()))
                .filter(execution -> status == null || status.isBlank() || status.equalsIgnoreCase(execution.getStatus()))
                .map(this::toResponse).toList();
    }

    public AutomationStartResponse retry(String executionId, String tenantKey, String siteKey, boolean fromFailedNode) {
        AutomationExecution source = scopedExecution(executionId, tenantKey, siteKey);
        if (!isTerminal(source.getStatus())) throw new IllegalArgumentException("only a terminal execution can be retried");
        Map<String, Object> context = new LinkedHashMap<>(source.getContext());
        context.remove("n8nState");
        context.remove("processedCallbacks");
        context.put("runMode", "RETRY");
        context.put("retryOf", source.getExecutionId());
        Map<String, Object> input = new LinkedHashMap<>(source.getInput());
        if (fromFailedNode) {
            source.getSteps().stream().filter(step -> "FAILED".equals(step.getStatus())).reduce((left, right) -> right).ifPresent(step -> {
                context.put("startNodeId", step.getNodeId());
                Object items = step.getInputSnapshot().get("items");
                if (items != null) input.put("items", items);
            });
        }
        Map<String, Object> inline = restoreMongoMap(source.getInlineFragment());
        return start(new AutomationStartRequest(
                source.getBlockKey(), source.getAutomationFlowKey(), AutomationExecutionMode.ASYNC, source.getFailurePolicy(),
                source.getCorrelationKey(), source.getCallbackPath(), source.getTenantKey(), source.getSiteKey(), input, context,
                inline, source.getMaxRetries(), source.getTimeoutSeconds(), 0L, source.getAutomationFlowKey(),
                source.getManagedObjectId(), null, input, inline
        ));
    }

    public AutomationStartResponse cancel(String executionId) {
        AutomationExecution execution = repository.findByExecutionId(executionId).orElseThrow();
        return cancel(execution);
    }

    public AutomationStartResponse cancel(String executionId, String tenantKey, String siteKey) {
        return cancel(scopedExecution(executionId, tenantKey, siteKey));
    }

    private AutomationStartResponse cancel(AutomationExecution execution) {
        execution.setCancelRequested(true);
        execution.setCancelledAt(Instant.now());
        if (!isTerminal(execution.getStatus())) {
            execution.setStatus("CANCELLED");
            execution.setCompletedAt(Instant.now());
            execution.setResumeAt(null);
            execution.setResumeNodeId(null);
        }
        execution.setUpdatedAt(Instant.now());
        execution.setSnapshot(buildSnapshot(execution, execution.getOutput(), execution.getStatus()));
        repository.save(execution);
        if (execution.getCallbackPath() != null && !execution.getCallbackPath().isBlank()) {
            callbackBpm(execution, execution.getCallbackPath(), execution.getContext());
        }
        return toResponse(execution);
    }

    private void executeNow(AutomationExecution execution) {
        AutomationExecution latest = repository.findByExecutionId(execution.getExecutionId()).orElse(execution);
        if (latest.isCancelRequested()) {
            latest.setStatus("CANCELLED");
            latest.setCancelledAt(firstInstant(latest.getCancelledAt(), Instant.now()));
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, Map.of(), "CANCELLED"));
            releaseLease(latest);
            repository.save(latest);
            if (latest.getCallbackPath() != null && !latest.getCallbackPath().isBlank()) {
                callbackBpm(latest, latest.getCallbackPath(), latest.getContext());
            }
            return;
        }
        if (latest.getTimeoutAt() != null && Instant.now().isAfter(latest.getTimeoutAt())) {
            latest.setStatus("TIMED_OUT");
            latest.setError(Map.of("message", "automation execution timed out"));
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, Map.of(), "TIMED_OUT"));
            releaseLease(latest);
            repository.save(latest);
            if (latest.getCallbackPath() != null && !latest.getCallbackPath().isBlank()) {
                callbackBpm(latest, latest.getCallbackPath(), latest.getContext());
            }
            return;
        }

        AutomationFlowDefinition executedDefinition = null;
        try {
            Map<String, Object> output;
            if (isGraphExecution(latest)) {
                executedDefinition = graphDefinition(latest);
                runGraph(latest, executedDefinition);
                output = latest.getOutput();
                if (isWaiting(latest.getStatus()) && latest.getExecutionMode() == AutomationExecutionMode.SYNC) {
                    throw new IllegalStateException("automation flows containing WAIT or WAIT_FOR_CALLBACK must run async");
                }
            } else {
                output = evaluateExecution(latest);
                latest.setOutput(output);
                latest.setStatus("COMPLETED");
                latest.setCompletedAt(Instant.now());
            }
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, output, latest.getStatus()));
            if (isWaiting(latest.getStatus()) || isTerminal(latest.getStatus())) releaseLease(latest);
            repository.save(latest);
        } catch (RuntimeException ex) {
            latest.setStatus("FAILED");
            latest.setError(Map.of("message", firstNonBlank(ex.getMessage(), "automation failed")));
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, Map.of(), "FAILED"));
            releaseLease(latest);
            repository.save(latest);
            startErrorWorkflow(latest, executedDefinition);
        }

        if (!isWaiting(latest.getStatus()) && latest.getCallbackPath() != null && !latest.getCallbackPath().isBlank()) {
            callbackBpm(latest, latest.getCallbackPath(), latest.getContext());
        }
    }

    public AutomationStartResponse acceptCallback(String executionId, String nodeId, String callbackId, Map<String, Object> payload) {
        AutomationExecution execution = repository.findByExecutionId(executionId).orElseThrow();
        if (isProcessedCallback(execution, callbackId, payload)) {
            return toResponse(execution);
        }
        if (!"WAITING_CALLBACK".equals(execution.getStatus()) || !Objects.equals(nodeId, execution.getCurrentNodeId())) {
            throw new IllegalArgumentException("execution is not waiting at callback node");
        }
        try {
            AutomationFlowDefinition definition = graphDefinition(execution);
            if (isN8nItems(definition)) itemStreamRuntime.callback(execution, definition, nodeId, callbackId, payload == null ? Map.of() : payload);
            else graphRuntime.callback(execution, definition, nodeId, callbackId, payload == null ? Map.of() : payload);
        }
        catch (RuntimeException ex) { execution.setStatus("FAILED"); execution.setError(Map.of("message", Objects.toString(ex.getMessage(), "callback resume failed"))); execution.setCompletedAt(Instant.now()); }
        execution.setUpdatedAt(Instant.now()); execution.setSnapshot(buildSnapshot(execution, execution.getOutput(), execution.getStatus())); repository.save(execution);
        if (!isWaiting(execution.getStatus()) && execution.getCallbackPath() != null) callbackBpm(execution, execution.getCallbackPath(), execution.getContext());
        return toResponse(execution);
    }

    public List<com.cyancoder.automationorchestrator.domain.AutomationExecutionStep> steps(String executionId) {
        return repository.findByExecutionId(executionId).orElseThrow().getSteps();
    }

    public List<com.cyancoder.automationorchestrator.domain.AutomationExecutionStep> steps(String executionId, String tenantKey, String siteKey) {
        return scopedExecution(executionId, tenantKey, siteKey).getSteps();
    }

    public List<Map<String, Object>> deadLetters(String executionId) {
        return repository.findByExecutionId(executionId).orElseThrow().getDeadLetters();
    }

    public List<Map<String, Object>> deadLetters(String executionId, String tenantKey, String siteKey) {
        return scopedExecution(executionId, tenantKey, siteKey).getDeadLetters();
    }

    public AutomationStartResponse requeueDeadLetter(String executionId, String deadLetterId) {
        AutomationExecution execution=repository.findByExecutionId(executionId).orElseThrow();
        return requeueDeadLetter(execution, deadLetterId);
    }

    public AutomationStartResponse requeueDeadLetter(String executionId, String deadLetterId, String tenantKey, String siteKey) {
        return requeueDeadLetter(scopedExecution(executionId, tenantKey, siteKey), deadLetterId);
    }

    private AutomationStartResponse requeueDeadLetter(AutomationExecution execution, String deadLetterId) {
        Map<String,Object> letter=execution.getDeadLetters().stream().filter(item->deadLetterId.equals(item.get("id"))).findFirst().orElseThrow();
        execution.getDeadLetters().remove(letter);execution.setCurrentNodeId(Objects.toString(letter.get("nodeId"),null));execution.setStatus("RUNNING");execution.setError(new LinkedHashMap<>());execution.setCompletedAt(null);
        try {
            AutomationFlowDefinition definition = graphDefinition(execution);
            if (isN8nItems(definition)) itemStreamRuntime.requeueDeadLetter(execution, definition, letter);
            else graphRuntime.run(execution, definition);
        }
        catch(RuntimeException ex){execution.setStatus("FAILED");execution.setError(Map.of("message",Objects.toString(ex.getMessage(),"requeue failed")));execution.setCompletedAt(Instant.now());}
        execution.setUpdatedAt(Instant.now());execution.setSnapshot(buildSnapshot(execution,execution.getOutput(),execution.getStatus()));repository.save(execution);
        return toResponse(execution);
    }

    public Map<String,Object> metrics() {
        return metrics(repository.findAll());
    }

    public Map<String,Object> metrics(String tenantKey, String siteKey) {
        return metrics(repository.findAllByTenantKeyAndSiteKey(scope(tenantKey), scope(siteKey)));
    }

    private Map<String,Object> metrics(List<AutomationExecution> all) {
        Map<String,Long> counts=all.stream().collect(java.util.stream.Collectors.groupingBy(AutomationExecution::getStatus,java.util.stream.Collectors.counting()));
        long completed=all.stream().filter(item->item.getCreatedAt()!=null&&item.getCompletedAt()!=null).count();long duration=all.stream().filter(item->item.getCreatedAt()!=null&&item.getCompletedAt()!=null).mapToLong(item->item.getCompletedAt().toEpochMilli()-item.getCreatedAt().toEpochMilli()).sum();
        return Map.of("executionCounts",counts,"deadLetters",all.stream().mapToLong(item->item.getDeadLetters().size()).sum(),"averageDurationMs",completed==0?0:duration/completed,"activeExecutions",counts.entrySet().stream().filter(item->List.of("RUNNING","WAITING","WAITING_CALLBACK","WAITING_CONCURRENCY").contains(item.getKey())).mapToLong(Map.Entry::getValue).sum());
    }

    @Scheduled(fixedDelayString = "${automation.worker.recovery-poll-ms:1000}")
    public void resumeDueExecutions() {
        if (graphRuntime == null && itemStreamRuntime == null) return;
        int claimed = 0;
        while (claimed++ < Math.max(1, workerProperties.getRecoveryBatchSize())) {
            Instant now = Instant.now();
            Optional<AutomationExecution> candidate = repository.claimNextRecoverable(
                    workerProperties.getId(),
                    now,
                    now.minus(workerProperties.getOrphanGrace()),
                    now.plus(workerProperties.getLeaseDuration())
            );
            if (candidate.isEmpty()) break;
            AutomationExecution execution = candidate.get();
            try {
                String previousStatus = execution.getStatus();
                String previousWorker = execution.getWorkerId();
                execution.setRevision(execution.getRevision() == null ? 1L : execution.getRevision() + 1);
                claimForLocalWorker(execution);
                if ("WAITING".equals(previousStatus) || "WAITING_CONCURRENCY".equals(previousStatus)) {
                    execution.setCurrentNodeId(execution.getResumeNodeId());
                    execution.setResumeAt(null);
                    execution.setResumeNodeId(null);
                } else {
                    Map<String, Object> context = new LinkedHashMap<>(execution.getContext());
                    context.put("recoveredAt", now.toString());
                    context.put("recoveredFromWorker", Objects.toString(previousWorker, "unknown"));
                    context.put("recoveryCount", AutomationDataSupport.longValue(context.get("recoveryCount"), 0) + 1);
                    execution.setContext(context);
                }
                repository.save(execution);
                executeNow(execution);
            } catch (RuntimeException ex) {
                execution.setStatus("FAILED");
                execution.setError(Map.of("message", Objects.toString(ex.getMessage(), "resume failed")));
                execution.setCompletedAt(Instant.now());
                execution.setUpdatedAt(Instant.now());
                execution.setSnapshot(buildSnapshot(execution, execution.getOutput(), execution.getStatus()));
                releaseLease(execution);
                repository.save(execution);
                if (execution.getCallbackPath() != null) {
                    callbackBpm(execution, execution.getCallbackPath(), execution.getContext());
                }
            }
        }
    }

    @Scheduled(fixedDelayString = "${automation.worker.heartbeat-ms:10000}")
    public void renewWorkerLeases() {
        Instant now = Instant.now();
        repository.renewLeases(workerProperties.getId(), now, now.plus(workerProperties.getLeaseDuration()));
    }

    private void callbackBpm(AutomationExecution execution, String callbackPath, Map<String, Object> context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("executionId", execution.getExecutionId());
        payload.put("blockKey", execution.getBlockKey());
        payload.put("status", execution.getStatus());
        payload.put("snapshot", execution.getSnapshot());
        payload.put("output", execution.getOutput());
        payload.putAll(execution.getOutput());
        if (execution.getError() != null && !execution.getError().isEmpty()) {
            payload.put("error", execution.getError());
        }

        BpmAsyncCallbackRequest callbackRequest = new BpmAsyncCallbackRequest(
                execution.getExecutionId(),
                execution.getStatus(),
                null,
                payload,
                context == null ? Map.of() : context
        );
        try {
            byte[] body = objectMapper.writeValueAsBytes(callbackRequest);
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            HttpHeaders headers = httpSupport.internalHeaders("bpm-service", execution.getTenantKey(), execution.getSiteKey());
            headers.set(callbackProperties.getTimestampHeader(), timestamp);
            headers.set(callbackProperties.getSignatureHeader(), sign(timestamp, body));
            httpSupport.exchange("bpm-service", callbackPath, HttpMethod.POST, callbackRequest, headers, Map.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to callback BPM automation result", ex);
        }
    }

    private Map<String, Object> evaluateExecution(AutomationExecution execution) {
        if (execution.getInlineFragment() != null && !execution.getInlineFragment().isEmpty()) {
            return evaluateInlineFragment(restoreMongoMap(execution.getInlineFragment()), execution.getInput(), execution.getTenantKey(), execution.getSiteKey());
        }
        return evaluateHybridScreening(execution.getInput());
    }

    private AutomationFlowDefinition resolveRequestedGraph(AutomationStartRequest request, Map<String, Object> inline) {
        if (flowDefinitionService == null || (graphRuntime == null && itemStreamRuntime == null)) return null;
        if (inline.containsKey("nodes")) {
            AutomationFlowDefinition definition = objectMapper.convertValue(inline, AutomationFlowDefinition.class);
            if (definition.getFlowKey() == null || definition.getFlowKey().isBlank()) definition.setFlowKey(firstNonBlank(request.flowKey(), request.automationFlowKey(), "inline-flow"));
            if (definition.getVersion() == null) definition.setVersion(1);
            flowDefinitionService.validate(definition);
            return definition;
        }
        String flowKey = firstNonBlank(request.flowKey(), request.automationFlowKey());
        if (flowKey != null && !"hybrid-screening-automation".equals(flowKey)) {
            String environment = request.context() == null ? null : string(request.context().get("environment"));
            return flowDefinitionService.active(request.tenantKey(), request.siteKey(), flowKey, firstNonBlank(environment, "default"));
        }
        return null;
    }

    private boolean isGraphExecution(AutomationExecution execution) {
        if (execution.getInlineFragment() == null || execution.getInlineFragment().isEmpty()) return false;
        return restoreMongoMap(execution.getInlineFragment()).containsKey("nodes");
    }

    private AutomationFlowDefinition graphDefinition(AutomationExecution execution) {
        return objectMapper.convertValue(restoreMongoMap(execution.getInlineFragment()), AutomationFlowDefinition.class);
    }

    private void runGraph(AutomationExecution execution, AutomationFlowDefinition definition) {
        if (isN8nItems(definition)) {
            if (itemStreamRuntime == null) throw new IllegalStateException("N8N_ITEMS runtime is unavailable");
            itemStreamRuntime.run(execution, definition);
        } else {
            if (graphRuntime == null) throw new IllegalStateException("VARIABLES runtime is unavailable");
            graphRuntime.run(execution, definition);
        }
    }

    private void startErrorWorkflow(AutomationExecution failed, AutomationFlowDefinition definition) {
        if (definition == null || definition.getErrorWorkflowKey() == null || definition.getErrorWorkflowKey().isBlank()) return;
        int depth = (int) AutomationDataSupport.longValue(failed.getContext().get("errorWorkflowDepth"), 0);
        if (depth >= 1) return;
        Map<String, Object> errorInput = new LinkedHashMap<>();
        errorInput.put("execution", Map.of(
                "id", failed.getExecutionId(),
                "flowKey", failed.getAutomationFlowKey(),
                "status", failed.getStatus(),
                "lastNodeId", Objects.toString(failed.getCurrentNodeId(), ""),
                "error", failed.getError()
        ));
        errorInput.put("workflow", Map.of("flowKey", definition.getFlowKey(), "version", definition.getVersion()));
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("runMode", "ERROR");
        context.put("errorWorkflowDepth", depth + 1);
        context.put("sourceExecutionId", failed.getExecutionId());
        try {
            start(new AutomationStartRequest(
                    definition.getErrorWorkflowKey(), definition.getErrorWorkflowKey(), AutomationExecutionMode.ASYNC,
                    AutomationFailurePolicy.MARK_FAILED, failed.getCorrelationKey(), null,
                    failed.getTenantKey(), failed.getSiteKey(), errorInput, context, null, 0, null, 0L,
                    definition.getErrorWorkflowKey(), failed.getManagedObjectId(),
                    "error:" + failed.getExecutionId(), errorInput, null
            ));
        } catch (RuntimeException errorWorkflowFailure) {
            Map<String, Object> error = new LinkedHashMap<>(failed.getError());
            error.put("errorWorkflowFailure", Objects.toString(errorWorkflowFailure.getMessage(), "error workflow failed to start"));
            failed.setError(error);
            repository.save(failed);
        }
    }

    private boolean isN8nItems(AutomationFlowDefinition definition) {
        return "N8N_ITEMS".equalsIgnoreCase(definition.getRuntimeMode());
    }

    private boolean isWaiting(String status) {
        return "WAITING".equalsIgnoreCase(status) || "WAITING_CALLBACK".equalsIgnoreCase(status) || "WAITING_CONCURRENCY".equalsIgnoreCase(status);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateInlineFragment(Map<String, Object> inlineFragment, Map<String, Object> input,
                                                       String tenantKey, String siteKey) {
        String type = string(inlineFragment.get("type"));
        if ("FAIL".equalsIgnoreCase(type)) {
            throw new IllegalStateException(firstNonBlank(string(inlineFragment.get("message")), "inline fragment failed"));
        }
        if ("MAP_OUTPUT".equalsIgnoreCase(type)) {
            Object output = inlineFragment.get("output");
            return output instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : Map.of();
        }
        if ("PIPELINE".equalsIgnoreCase(type)) {
            return pipelineRuntime.execute(inlineFragment, input, tenantKey, siteKey);
        }
        if ("HYBRID_SCREENING".equalsIgnoreCase(type) || type == null || type.isBlank()) {
            return evaluateHybridScreening(input);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fragmentType", type);
        result.put("accepted", true);
        result.put("inputEcho", input);
        return result;
    }

    private Map<String, Object> evaluateHybridScreening(Map<String, Object> input) {
        double amount = number(input.get("requestedAmount"));
        String nationalId = string(input.get("nationalId"));
        String fullName = firstNonBlank(string(input.get("fullName")), "Applicant");
        int riskScore = (int) Math.max(5, Math.min(95, Math.round((amount / 1000.0) + (nationalId == null ? 30 : nationalId.length()))));
        String screeningRoute;
        if (riskScore >= 70) {
            screeningRoute = "REJECT";
        } else if (riskScore >= 40) {
            screeningRoute = "MANUAL_REVIEW";
        } else {
            screeningRoute = "FAST_TRACK";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("riskScore", riskScore);
        result.put("screeningRoute", screeningRoute);
        result.put("externalRef", "screen-" + slug(fullName) + "-" + UUID.randomUUID().toString().substring(0, 8));
        result.put("providerDecision", screeningRoute);
        return result;
    }

    private Map<String, Object> buildSnapshot(AutomationExecution execution, Map<String, Object> output, String status) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("executionId", execution.getExecutionId());
        snapshot.put("blockKey", execution.getBlockKey());
        snapshot.put("automationFlowKey", execution.getAutomationFlowKey());
        snapshot.put("managedObjectId", execution.getManagedObjectId());
        snapshot.put("idempotencyKey", execution.getIdempotencyKey());
        snapshot.put("executionMode", execution.getExecutionMode().name());
        snapshot.put("failurePolicy", execution.getFailurePolicy().name());
        snapshot.put("status", status);
        snapshot.put("input", execution.getInput());
        snapshot.put("output", output);
        snapshot.put("error", execution.getError());
        snapshot.put("retryCount", execution.getRetryCount());
        snapshot.put("completedAt", execution.getCompletedAt() == null ? null : execution.getCompletedAt().toString());
        return snapshot;
    }

    private AutomationStartResponse toResponse(AutomationExecution execution) {
        return new AutomationStartResponse(
                execution.getExecutionId(),
                execution.getBlockKey(),
                execution.getAutomationFlowKey(),
                execution.getManagedObjectId(),
                execution.getIdempotencyKey(),
                execution.getCorrelationKey(),
                execution.getStatus(),
                execution.getSnapshot(),
                execution.getOutput(),
                execution.getError()
        );
    }

    private String sign(String timestamp, byte[] canonicalBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(callbackProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '.');
            mac.update(canonicalBody);
            return HexFormat.of().formatHex(mac.doFinal());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign automation callback", ex);
        }
    }

    private double number(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> firstMap(Map<String, Object> primary, Map<String, Object> fallback) {
        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        return fallback == null ? Map.of() : fallback;
    }

    private Map<String, Object> mongoSafeMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(
                String.valueOf(key).replace(".", MONGO_DOT_ESCAPE),
                transformMapValue(value, true)
        ));
        return result;
    }

    private Map<String, Object> restoreMongoMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(
                String.valueOf(key).replace(MONGO_DOT_ESCAPE, "."),
                transformMapValue(value, false)
        ));
        return result;
    }

    private Object transformMapValue(Object value, boolean mongoSafe) {
        if (value instanceof Map<?, ?> map) {
            return mongoSafe ? mongoSafeMap(map) : restoreMongoMap(map);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            iterable.forEach(item -> result.add(transformMapValue(item, mongoSafe)));
            return result;
        }
        return value;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isTerminalFailure(String status) {
        return "FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) || "TIMED_OUT".equalsIgnoreCase(status);
    }

    private boolean isTerminal(String status) {
        return "COMPLETED".equalsIgnoreCase(status) || isTerminalFailure(status);
    }

    private boolean isProcessedCallback(AutomationExecution execution, String callbackId, Map<String, Object> payload) {
        Object callbacks = execution.getContext().get("processedCallbacks");
        if (!(callbacks instanceof Iterable<?> processed)) {
            return false;
        }
        String key = callbackId == null || callbackId.isBlank()
                ? "payload:" + Objects.hashCode(payload)
                : callbackId;
        for (Object processedKey : processed) {
            if (key.equals(processedKey)) {
                return true;
            }
        }
        return false;
    }

    private Instant firstInstant(Instant left, Instant right) {
        return left == null ? right : left;
    }

    private void claimForLocalWorker(AutomationExecution execution) {
        Instant now = Instant.now();
        execution.setStatus("RUNNING");
        execution.setWorkerId(workerProperties.getId());
        execution.setHeartbeatAt(now);
        execution.setLeaseUntil(now.plus(workerProperties.getLeaseDuration()));
        execution.setUpdatedAt(now);
    }

    private void releaseLease(AutomationExecution execution) {
        execution.setWorkerId(null);
        execution.setLeaseUntil(null);
        execution.setHeartbeatAt(null);
    }

    private AutomationExecution scopedExecution(String executionId, String tenantKey, String siteKey) {
        return repository.findFirstByExecutionIdAndTenantKeyAndSiteKey(executionId, scope(tenantKey), scope(siteKey)).orElseThrow();
    }

    private String scope(String value) {
        return value == null || value.isBlank() ? "default" : value.trim();
    }

    private AutomationStartRequest withScope(AutomationStartRequest request, String tenantKey, String siteKey) {
        return new AutomationStartRequest(request.blockKey(), request.automationFlowKey(), request.executionMode(), request.failurePolicy(),
                request.correlationKey(), request.callbackPath(), scope(tenantKey), scope(siteKey), request.input(), request.context(),
                request.inlineFragment(), request.maxRetries(), request.timeoutSeconds(), request.delayMillis(), request.flowKey(),
                request.managedObjectId(), request.idempotencyKey(), request.variables(), request.inlineFlow());
    }

    private String slug(String value) {
        return value == null ? "applicant" : value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
