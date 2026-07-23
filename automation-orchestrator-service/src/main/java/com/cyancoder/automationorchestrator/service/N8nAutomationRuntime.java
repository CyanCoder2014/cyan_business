package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationEdge;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionMode;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionStep;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class N8nAutomationRuntime {
    private static final String STATE_KEY = "n8nState";
    private static final String CALLBACKS_KEY = "processedCallbacks";
    private static final int MAX_NODE_RUNS = 10_000;

    private final InternalServiceHttpSupport http;
    private final ConnectorCredentialService credentials;
    private final AutomationFlowDefinitionService flows;
    private final AutomationExecutionRepository executions;
    private final ObjectMapper objectMapper;
    private final N8nExpressionService expressions;
    private final String defaultScriptRunnerUrl;
    private final AutomationExecutionCheckpointService checkpoints;

    public N8nAutomationRuntime(InternalServiceHttpSupport http,
                                ConnectorCredentialService credentials,
                                AutomationFlowDefinitionService flows,
                                AutomationExecutionRepository executions,
                                ObjectMapper objectMapper,
                                N8nExpressionService expressions,
                                @Value("${automation.script-runner.url:}") String defaultScriptRunnerUrl) {
        this(http, credentials, flows, executions, objectMapper, expressions, defaultScriptRunnerUrl, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public N8nAutomationRuntime(InternalServiceHttpSupport http,
                                ConnectorCredentialService credentials,
                                AutomationFlowDefinitionService flows,
                                AutomationExecutionRepository executions,
                                ObjectMapper objectMapper,
                                N8nExpressionService expressions,
                                @Value("${automation.script-runner.url:}") String defaultScriptRunnerUrl,
                                AutomationExecutionCheckpointService checkpoints) {
        this.http = http;
        this.credentials = credentials;
        this.flows = flows;
        this.executions = executions;
        this.objectMapper = objectMapper;
        this.expressions = expressions;
        this.defaultScriptRunnerUrl = defaultScriptRunnerUrl;
        this.checkpoints = checkpoints;
    }

    public void run(AutomationExecution execution, AutomationFlowDefinition definition) {
        Map<String, AutomationNode> nodes = nodeMap(definition);
        Map<String, Object> state = state(execution);
        List<Map<String, Object>> queue = queue(state);
        if (queue.isEmpty() && !AutomationDataSupport.bool(state.get("initialized"), false)) {
            String startNodeId = Objects.toString(execution.getContext().getOrDefault("startNodeId", definition.getEntryNodeId()));
            if (!nodes.containsKey(startNodeId)) throw new IllegalArgumentException("partial execution start node not found: " + startNodeId);
            queue.add(work(startNodeId, "0", initialItems(execution), null));
            state.put("initialized", true);
        }

        int runs = (int) AutomationDataSupport.longValue(state.get("nodeRuns"), 0);
        while (!queue.isEmpty()) {
            if (++runs > MAX_NODE_RUNS) throw new IllegalStateException("n8n workflow exceeded " + MAX_NODE_RUNS + " node executions");
            state.put("nodeRuns", runs);
            if (execution.isCancelRequested()) {
                execution.setStatus("CANCELLED");
                execution.setCompletedAt(Instant.now());
                return;
            }
            Map<String, Object> work = queue.removeFirst();
            String nodeId = Objects.toString(work.get("nodeId"), null);
            AutomationNode node = nodes.get(nodeId);
            if (node == null) throw new IllegalArgumentException("n8n node not found: " + nodeId);
            List<Map<String, Object>> items = items(work.get("items"));
            execution.setCurrentNodeId(nodeId);

            if (!node.isEnabled()) {
                route(definition, queue, node, List.of(new Emission("0", items)), nodeId);
                continue;
            }
            List<Map<String, Object>> pinned = pinnedItems(definition, node, execution);
            NodeOutcome outcome = null;
            int attempts = node.retryPolicy() == null || node.retryPolicy().maxAttempts() == null
                    ? 1 : Math.max(1, node.retryPolicy().maxAttempts());
            for (int attempt = 1; attempt <= attempts; attempt++) {
                AutomationExecutionStep step = begin(execution, node, items, attempt);
                try {
                    outcome = pinned == null ? execute(execution, definition, state, node, work, items) : NodeOutcome.output("0", pinned);
                    finish(step, outcome.waitStatus() == null ? "COMPLETED" : outcome.waitStatus(), outcome.emissions(), null);
                    break;
                } catch (RuntimeException failure) {
                    if (attempt < attempts) {
                        finish(step, "RETRYING", List.of(), Map.of("message", Objects.toString(failure.getMessage(), "node failed")));
                        backoff(node, attempt);
                    } else {
                        outcome = handleFailure(execution, definition, node, items, step, failure);
                    }
                }
            }

            if (outcome.callbackWait()) {
                state.put("waitingWork", work);
                state.put("waitingNodeId", node.id());
                execution.setStatus("WAITING_CALLBACK");
                persistOutput(execution, state);
                checkpoint(execution);
                return;
            }
            rememberOutputs(state, node, outcome.emissions());
            route(definition, queue, node, outcome.emissions(), nodeId);
            if (outcome.timedWait()) {
                execution.setStatus("WAITING");
                persistOutput(execution, state);
                checkpoint(execution);
                return;
            }
            persistOutput(execution, state);
            checkpoint(execution);
        }

        execution.setStatus("COMPLETED");
        execution.setCurrentNodeId(null);
        execution.setCompletedAt(Instant.now());
        persistOutput(execution, state);
        checkpoint(execution);
    }

    public void callback(AutomationExecution execution, AutomationFlowDefinition definition, String nodeId,
                         String callbackId, Map<String, Object> payload) {
        String callbackKey = callbackId == null || callbackId.isBlank()
                ? "payload:" + Objects.hashCode(payload)
                : callbackId;
        List<Object> processed = new ArrayList<>(AutomationDataSupport.list(execution.getContext().get(CALLBACKS_KEY)));
        if (processed.contains(callbackKey)) return;
        Map<String, Object> state = state(execution);
        if (!"WAITING_CALLBACK".equals(execution.getStatus()) || !Objects.equals(nodeId, state.get("waitingNodeId"))) {
            throw new IllegalArgumentException("execution is not waiting at n8n callback node");
        }
        AutomationNode node = nodeMap(definition).get(nodeId);
        Map<String, Object> waitingWork = AutomationDataSupport.map(state.remove("waitingWork"));
        state.remove("waitingNodeId");
        List<Map<String, Object>> resumed = items(waitingWork.get("items"));
        resumed = applyCallback(node, resumed, payload == null ? Map.of() : payload);
        processed.add(callbackKey);
        execution.getContext().put(CALLBACKS_KEY, processed);
        List<Emission> callbackOutput = List.of(new Emission("callback", resumed));
        rememberOutputs(state, node, callbackOutput);
        route(definition, queue(state), node, callbackOutput, nodeId);
        execution.setStatus("RUNNING");
        execution.setResumeAt(null);
        execution.setResumeNodeId(null);
        run(execution, definition);
    }

    public void requeueDeadLetter(AutomationExecution execution, AutomationFlowDefinition definition,
                                  Map<String, Object> deadLetter) {
        String nodeId = Objects.toString(deadLetter.get("nodeId"), null);
        if (!nodeMap(definition).containsKey(nodeId)) throw new IllegalArgumentException("dead-letter node no longer exists");
        Map<String, Object> state = state(execution);
        queue(state).add(0, work(nodeId, "0", items(deadLetter.get("items")), null));
        execution.setStatus("RUNNING");
        execution.setError(new LinkedHashMap<>());
        execution.setCompletedAt(null);
        run(execution, definition);
    }

    private NodeOutcome execute(AutomationExecution execution, AutomationFlowDefinition definition,
                                Map<String, Object> state, AutomationNode node, Map<String, Object> work,
                                List<Map<String, Object>> items) {
        Map<String, Object> config = node.configOrEmpty();
        return switch (node.type()) {
            case WEBHOOK_TRIGGER, MANUAL_TRIGGER, SCHEDULE_TRIGGER, ERROR_TRIGGER, NO_OP, END -> NodeOutcome.output("0", items);
            case EDIT_FIELDS, MAP_FIELDS -> NodeOutcome.output("0", editFields(execution, definition, state, config, items));
            case JSON_TRANSFORM -> NodeOutcome.output("0", transform(execution, definition, state, config, items));
            case IF -> splitIf(execution, definition, state, config, items);
            case FILTER -> NodeOutcome.output("0", filter(execution, definition, state, config, items));
            case SWITCH -> splitSwitch(execution, definition, state, config, items);
            case SPLIT_OUT -> NodeOutcome.output("0", splitOut(config, items));
            case AGGREGATE -> NodeOutcome.output("0", aggregate(config, items));
            case SORT -> NodeOutcome.output("0", sort(config, items));
            case LIMIT -> NodeOutcome.output("0", limit(config, items));
            case DEDUP_BY_KEY, REMOVE_DUPLICATES -> NodeOutcome.output("0", deduplicate(config, items));
            case MERGE -> merge(definition, state, node, work, items);
            case LOOP_OVER_ITEMS, FOR_EACH -> loopOverItems(state, node, config, items);
            case CALL_API, HTTP_REQUEST, N8N_WORKFLOW -> NodeOutcome.output("0", request(execution, definition, state, node, config, items));
            case CODE -> NodeOutcome.output("0", code(execution, definition, state, node, config, items));
            case WAIT -> waitForTime(execution, config, items);
            case WAIT_FOR_CALLBACK -> NodeOutcome.callback(items);
            case EXECUTION_DATA -> NodeOutcome.output("0", executionData(execution, definition, state, config, items));
            case RESPOND_TO_WEBHOOK -> NodeOutcome.output("0", respond(execution, definition, state, config, items));
            case STOP_AND_ERROR -> throw stopError(execution, definition, state, config, items);
            case SUBFLOW, EXECUTE_WORKFLOW -> executeSubflow(execution, definition, state, node, work, config, items);
            case FILE_METADATA -> NodeOutcome.output("0", fileMetadata(config, items));
            case JDM_DECISION, PAGINATED_CALL_API -> throw new IllegalArgumentException(node.type() + " uses the VARIABLES runtime; use HTTP_REQUEST/CODE in N8N_ITEMS mode");
        };
    }

    private NodeOutcome handleFailure(AutomationExecution execution, AutomationFlowDefinition definition,
                                      AutomationNode node, List<Map<String, Object>> items,
                                      AutomationExecutionStep step, RuntimeException failure) {
        finish(step, "FAILED", List.of(), Map.of("message", Objects.toString(failure.getMessage(), "node failed")));
        if (node.errorPolicy() != null && Boolean.TRUE.equals(node.errorPolicy().deadLetterOnFailure())) {
            execution.getDeadLetters().add(new LinkedHashMap<>(Map.of(
                    "id", "dlq-" + UUID.randomUUID(),
                    "nodeId", node.id(),
                    "reason", Objects.toString(failure.getMessage(), "failed"),
                    "createdAt", Instant.now().toString(),
                    "items", items
            )));
        }
        if (node.errorPolicy() != null && Boolean.TRUE.equals(node.errorPolicy().continueOnFail())) {
            List<Map<String, Object>> errorItems = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Map<String, Object> copy = copyItem(item);
                json(copy).put("error", Map.of("message", Objects.toString(failure.getMessage(), "node failed"), "node", Objects.toString(node.name(), node.id())));
                errorItems.add(copy);
            }
            return NodeOutcome.output("0", errorItems);
        }
        if (node.errorPolicy() != null && node.errorPolicy().fallbackNodeId() != null) {
            return NodeOutcome.output("fallback:" + node.errorPolicy().fallbackNodeId(), items);
        }
        throw failure;
    }

    private List<Map<String, Object>> editFields(AutomationExecution execution, AutomationFlowDefinition definition,
                                                  Map<String, Object> state, Map<String, Object> config,
                                                  List<Map<String, Object>> items) {
        Map<String, Object> assignments = AutomationDataSupport.map(config.getOrDefault("assignments", config.get("mappings")));
        boolean keepOnlySet = AutomationDataSupport.bool(config.get("keepOnlySet"), false);
        List<Map<String, Object>> output = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> source = items.get(index);
            Map<String, Object> target = keepOnlySet ? item(Map.of()) : copyItem(source);
            N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, items, source, index);
            assignments.forEach((path, value) -> AutomationDataSupport.setPath(json(target), path,
                    expressions.materialize(value, evaluation)));
            output.add(target);
        }
        return output;
    }

    private List<Map<String, Object>> transform(AutomationExecution execution, AutomationFlowDefinition definition,
                                                 Map<String, Object> state, Map<String, Object> config,
                                                 List<Map<String, Object>> items) {
        List<Map<String, Object>> output = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> source = items.get(index);
            Object transformed = expressions.materialize(config.getOrDefault("template", config.get("sourcePath")),
                    evaluation(execution, definition, state, items, source, index));
            Map<String, Object> target = copyItem(source);
            String path = Objects.toString(config.getOrDefault("targetPath", "transformed"));
            AutomationDataSupport.setPath(json(target), path, transformed);
            output.add(target);
        }
        return output;
    }

    private NodeOutcome splitIf(AutomationExecution execution, AutomationFlowDefinition definition,
                                Map<String, Object> state, Map<String, Object> config,
                                List<Map<String, Object>> items) {
        List<Map<String, Object>> yes = new ArrayList<>();
        List<Map<String, Object>> no = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> item = items.get(index);
            N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, items, item, index);
            Object left = expressions.materialize(config.get("field"), evaluation);
            Object right = expressions.materialize(config.get("value"), evaluation);
            (AutomationDataSupport.compare(left, Objects.toString(config.get("operator"), "EQ"), right) ? yes : no).add(item);
        }
        return new NodeOutcome(List.of(new Emission("true", yes), new Emission("false", no)), null, false);
    }

    private List<Map<String, Object>> filter(AutomationExecution execution, AutomationFlowDefinition definition,
                                              Map<String, Object> state, Map<String, Object> config,
                                              List<Map<String, Object>> items) {
        return splitIf(execution, definition, state, config, items).emissions().getFirst().items();
    }

    private NodeOutcome splitSwitch(AutomationExecution execution, AutomationFlowDefinition definition,
                                    Map<String, Object> state, Map<String, Object> config,
                                    List<Map<String, Object>> items) {
        Map<String, Object> cases = AutomationDataSupport.map(config.get("cases"));
        Map<String, List<Map<String, Object>>> ports = new LinkedHashMap<>();
        cases.keySet().forEach(port -> ports.put(port, new ArrayList<>()));
        ports.put("default", new ArrayList<>());
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> item = items.get(index);
            N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, items, item, index);
            Object actual = expressions.materialize(config.get("field"), evaluation);
            String selected = cases.entrySet().stream()
                    .filter(entry -> Objects.equals(normalize(actual), normalize(expressions.materialize(entry.getValue(), evaluation))))
                    .map(Map.Entry::getKey).findFirst().orElse("default");
            ports.get(selected).add(item);
        }
        return new NodeOutcome(ports.entrySet().stream().map(entry -> new Emission(entry.getKey(), entry.getValue())).toList(), null, false);
    }

    private List<Map<String, Object>> splitOut(Map<String, Object> config, List<Map<String, Object>> items) {
        String field = Objects.toString(config.getOrDefault("field", config.getOrDefault("sourcePath", "items")));
        String target = Objects.toString(config.getOrDefault("targetField", field));
        List<Map<String, Object>> output = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> source = items.get(index);
            for (Object value : AutomationDataSupport.list(AutomationDataSupport.readPath(json(source), field))) {
                Map<String, Object> split = copyItem(source);
                AutomationDataSupport.setPath(json(split), target, value);
                split.put("pairedItem", Map.of("item", index, "input", 0));
                output.add(split);
            }
        }
        return output;
    }

    private List<Map<String, Object>> aggregate(Map<String, Object> config, List<Map<String, Object>> items) {
        String field = AutomationDataSupport.string(config.get("field"));
        String target = Objects.toString(config.getOrDefault("targetField", "data"));
        List<Object> values = items.stream().map(item -> field == null ? json(item) : AutomationDataSupport.readPath(json(item), field)).toList();
        Map<String, Object> result = item(Map.of());
        AutomationDataSupport.setPath(json(result), target, values);
        List<Map<String, Object>> links = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) links.add(Map.of("item", index, "input", 0));
        result.put("pairedItem", links);
        return List.of(result);
    }

    private List<Map<String, Object>> sort(Map<String, Object> config, List<Map<String, Object>> items) {
        String field = Objects.toString(config.getOrDefault("field", ""));
        boolean descending = "DESC".equalsIgnoreCase(Objects.toString(config.getOrDefault("direction", "ASC")));
        List<Map<String, Object>> output = new ArrayList<>(items);
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                item -> Objects.toString(AutomationDataSupport.readPath(json(item), field), ""),
                Comparator.naturalOrder());
        output.sort(descending ? comparator.reversed() : comparator);
        return output;
    }

    private List<Map<String, Object>> limit(Map<String, Object> config, List<Map<String, Object>> items) {
        int max = (int) Math.max(0, AutomationDataSupport.longValue(config.getOrDefault("maxItems", config.get("limit")), 1));
        boolean fromEnd = "END".equalsIgnoreCase(Objects.toString(config.getOrDefault("keep", "FIRST")));
        if (items.size() <= max) return items;
        return new ArrayList<>(fromEnd ? items.subList(items.size() - max, items.size()) : items.subList(0, max));
    }

    private List<Map<String, Object>> deduplicate(Map<String, Object> config, List<Map<String, Object>> items) {
        String field = Objects.toString(config.getOrDefault("keyPath", config.getOrDefault("field", "")));
        boolean keepLast = "LAST".equalsIgnoreCase(Objects.toString(config.getOrDefault("keep", "FIRST")));
        Map<Object, Map<String, Object>> unique = new LinkedHashMap<>();
        for (Map<String, Object> item : items) {
            Object key = field.isBlank() ? json(item) : AutomationDataSupport.readPath(json(item), field);
            if (!keepLast && unique.containsKey(key)) continue;
            unique.put(key, item);
        }
        return new ArrayList<>(unique.values());
    }

    private NodeOutcome merge(AutomationFlowDefinition definition, Map<String, Object> state, AutomationNode node,
                              Map<String, Object> work, List<Map<String, Object>> items) {
        Map<String, Object> merges = AutomationDataSupport.map(state.get("merges"));
        String key = safeKey(node.id());
        List<Object> arrivals = new ArrayList<>(AutomationDataSupport.list(merges.get(key)));
        arrivals.add(Map.of("inputPort", Objects.toString(work.getOrDefault("inputPort", "0")), "items", items));
        int expected = (int) definition.getEdges().stream().filter(edge -> node.id().equals(edge.toNodeId())).count();
        if (arrivals.size() < Math.max(1, expected)) {
            merges.put(key, arrivals);
            state.put("merges", merges);
            return new NodeOutcome(List.of(), null, false);
        }
        merges.remove(key);
        state.put("merges", merges);
        String mode = Objects.toString(node.configOrEmpty().getOrDefault("mode", "APPEND")).toUpperCase(Locale.ROOT);
        List<List<Map<String, Object>>> streams = arrivals.stream()
                .map(arrival -> items(AutomationDataSupport.map(arrival).get("items"))).toList();
        List<Map<String, Object>> output = switch (mode) {
            case "COMBINE_BY_POSITION" -> combineByPosition(streams);
            case "COMBINE_BY_FIELD" -> combineByField(streams, Objects.toString(node.configOrEmpty().get("field"), "id"));
            case "CHOOSE_BRANCH" -> streams.get(Math.min((int) AutomationDataSupport.longValue(node.configOrEmpty().get("input"), 0), streams.size() - 1));
            default -> streams.stream().flatMap(List::stream).toList();
        };
        return NodeOutcome.output("0", output);
    }

    private List<Map<String, Object>> combineByPosition(List<List<Map<String, Object>>> streams) {
        int size = streams.stream().mapToInt(List::size).max().orElse(0);
        List<Map<String, Object>> output = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            Map<String, Object> combined = item(Map.of());
            for (List<Map<String, Object>> stream : streams) if (index < stream.size()) json(combined).putAll(json(stream.get(index)));
            output.add(combined);
        }
        return output;
    }

    private List<Map<String, Object>> combineByField(List<List<Map<String, Object>>> streams, String field) {
        Map<Object, Map<String, Object>> indexed = new LinkedHashMap<>();
        for (List<Map<String, Object>> stream : streams) for (Map<String, Object> item : stream) {
            Object key = AutomationDataSupport.readPath(json(item), field);
            Map<String, Object> combined = indexed.computeIfAbsent(key, ignored -> item(Map.of()));
            json(combined).putAll(json(item));
        }
        return new ArrayList<>(indexed.values());
    }

    private NodeOutcome loopOverItems(Map<String, Object> state, AutomationNode node, Map<String, Object> config,
                                      List<Map<String, Object>> items) {
        int batchSize = (int) Math.max(1, AutomationDataSupport.longValue(config.getOrDefault("batchSize", config.get("chunkSize")), 1));
        Map<String, Object> loops = AutomationDataSupport.map(state.get("loops"));
        String key = safeKey(node.id());
        Map<String, Object> loop = AutomationDataSupport.map(loops.get(key));
        if (loop.isEmpty()) {
            loop.put("remaining", new ArrayList<>(items));
            loop.put("processed", new ArrayList<>());
        } else {
            List<Object> processed = new ArrayList<>(AutomationDataSupport.list(loop.get("processed")));
            processed.addAll(items);
            loop.put("processed", processed);
        }
        List<Map<String, Object>> remaining = items(loop.get("remaining"));
        if (remaining.isEmpty()) {
            List<Map<String, Object>> processed = items(loop.get("processed"));
            loops.remove(key);
            state.put("loops", loops);
            return NodeOutcome.output("done", processed);
        }
        List<Map<String, Object>> batch = new ArrayList<>(remaining.subList(0, Math.min(batchSize, remaining.size())));
        loop.put("remaining", new ArrayList<>(remaining.subList(batch.size(), remaining.size())));
        loops.put(key, loop);
        state.put("loops", loops);
        return NodeOutcome.output("loop", batch);
    }

    private List<Map<String, Object>> request(AutomationExecution execution, AutomationFlowDefinition definition,
                                               Map<String, Object> state, AutomationNode node,
                                               Map<String, Object> config, List<Map<String, Object>> items) {
        List<Map<String, Object>> output = new ArrayList<>();
        boolean executeOnce = AutomationDataSupport.bool(config.get("executeOnce"), false);
        List<Map<String, Object>> selected = executeOnce && !items.isEmpty() ? List.of(items.getFirst()) : items;
        for (int index = 0; index < selected.size(); index++) {
            Map<String, Object> source = selected.get(index);
            N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, selected, source, index);
            Map<String, Object> resolved = AutomationDataSupport.map(expressions.materialize(config, evaluation));
            String serviceKey = AutomationDataSupport.string(resolved.get("serviceKey"));
            String path = withQuery(AutomationDataSupport.string(resolved.get("path")), resolved.get("query"));
            String url = withQuery(AutomationDataSupport.string(resolved.get(node.type() == AutomationNodeType.N8N_WORKFLOW ? "webhookUrl" : "url")), resolved.get("query"));
            HttpMethod method = HttpMethod.valueOf(Objects.toString(resolved.getOrDefault("method", "GET")).toUpperCase(Locale.ROOT));
            HttpHeaders headers = new HttpHeaders();
            AutomationDataSupport.map(resolved.get("headers")).forEach((key, value) -> headers.set(key, Objects.toString(value, "")));
            applyCredential(execution, node, headers);
            Object body = requestBody(resolved);
            Object response;
            if (serviceKey != null && path != null) {
                HttpHeaders internal = http.internalHeaders(serviceKey, execution.getTenantKey(), execution.getSiteKey());
                internal.addAll(headers);
                response = http.exchange(serviceKey, path, method, body, internal, Object.class);
            } else {
                if (url == null) throw new IllegalArgumentException("HTTP_REQUEST requires url or serviceKey/path");
                response = http.exchangeUrl(url, method, body, headers,
                        node.timeoutPolicy() == null ? null : node.timeoutPolicy().connectTimeoutMs(),
                        node.timeoutPolicy() == null ? null : node.timeoutPolicy().readTimeoutMs(), Object.class);
            }
            Map<String, Object> target = copyItem(source);
            String responsePath = Objects.toString(resolved.getOrDefault("responsePath", resolved.getOrDefault("storeResponseAt", "response")));
            AutomationDataSupport.setPath(json(target), responsePath, response);
            output.add(target);
        }
        return output;
    }

    private String withQuery(String address, Object rawQuery) {
        Map<String, Object> query = AutomationDataSupport.map(rawQuery);
        if (address == null || query.isEmpty()) return address;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(address);
        query.forEach((key, value) -> {
            List<Object> values = AutomationDataSupport.list(value);
            if (values.isEmpty()) builder.queryParam(key, value);
            else values.forEach(item -> builder.queryParam(key, item));
        });
        return builder.build().encode().toUriString();
    }

    private Object requestBody(Map<String, Object> config) {
        if (config.get("body") != null) return config.get("body");
        Object jsonBody = config.get("jsonBody");
        if (!(jsonBody instanceof String text)) return jsonBody;
        try { return objectMapper.readValue(text, Object.class); }
        catch (Exception ignored) { return text; }
    }

    private List<Map<String, Object>> code(AutomationExecution execution, AutomationFlowDefinition definition,
                                            Map<String, Object> state, AutomationNode node,
                                            Map<String, Object> config, List<Map<String, Object>> items) {
        String code = AutomationDataSupport.string(config.get("code"));
        String runnerUrl = Objects.toString(config.getOrDefault("runnerUrl", defaultScriptRunnerUrl), "");
        if (code != null && !code.isBlank()) {
            if (runnerUrl.isBlank()) throw new IllegalArgumentException("CODE JavaScript/Python requires automation.script-runner.url or config.runnerUrl");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            applyCredential(execution, node, headers);
            Object response = http.exchangeUrl(runnerUrl, HttpMethod.POST, Map.of(
                    "language", Objects.toString(config.getOrDefault("language", "javascript")),
                    "mode", Objects.toString(config.getOrDefault("mode", "RUN_ONCE_FOR_ALL_ITEMS")),
                    "code", code,
                    "items", items,
                    "variables", execution.getInput()
            ), headers, null, node.timeoutPolicy() == null ? null : node.timeoutPolicy().readTimeoutMs(), Object.class);
            Map<String, Object> result = AutomationDataSupport.map(response);
            return items(result.getOrDefault("items", response));
        }
        Object expression = config.get("expression");
        if (expression == null) throw new IllegalArgumentException("CODE requires expression or code");
        String targetPath = Objects.toString(config.getOrDefault("targetPath", "codeResult"));
        List<Map<String, Object>> output = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> target = copyItem(items.get(index));
            Object result = expressions.materialize(expression, evaluation(execution, definition, state, items, items.get(index), index));
            AutomationDataSupport.setPath(json(target), targetPath, result);
            output.add(target);
        }
        return output;
    }

    private NodeOutcome waitForTime(AutomationExecution execution, Map<String, Object> config, List<Map<String, Object>> items) {
        Instant resumeAt = AutomationDataSupport.instant(config.get("resumeAt"));
        if (resumeAt == null) resumeAt = Instant.now().plusSeconds(Math.max(1, AutomationDataSupport.longValue(config.get("delaySeconds"), 60)));
        execution.setResumeAt(resumeAt);
        execution.setResumeNodeId("n8n-queue");
        return new NodeOutcome(List.of(new Emission("0", items)), "WAITING", false);
    }

    private List<Map<String, Object>> executionData(AutomationExecution execution, AutomationFlowDefinition definition,
                                                     Map<String, Object> state, Map<String, Object> config,
                                                     List<Map<String, Object>> items) {
        Map<String, Object> custom = AutomationDataSupport.map(state.get("customData"));
        for (int index = 0; index < items.size(); index++) {
            N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, items, items.get(index), index);
            AutomationDataSupport.map(config.get("data")).forEach((key, value) -> custom.put(key, expressions.materialize(value, evaluation)));
        }
        state.put("customData", custom);
        return items;
    }

    private List<Map<String, Object>> respond(AutomationExecution execution, AutomationFlowDefinition definition,
                                               Map<String, Object> state, Map<String, Object> config,
                                               List<Map<String, Object>> items) {
        Map<String, Object> response = new LinkedHashMap<>();
        N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, items,
                items.isEmpty() ? item(Map.of()) : items.getFirst(), 0);
        response.put("statusCode", expressions.materialize(config.getOrDefault("statusCode", 200), evaluation));
        response.put("headers", expressions.materialize(config.getOrDefault("headers", Map.of()), evaluation));
        response.put("body", expressions.materialize(config.getOrDefault("body", items.isEmpty() ? Map.of() : json(items.getFirst())), evaluation));
        state.put("webhookResponse", response);
        return items;
    }

    private IllegalStateException stopError(AutomationExecution execution, AutomationFlowDefinition definition,
                                             Map<String, Object> state, Map<String, Object> config,
                                             List<Map<String, Object>> items) {
        N8nExpressionService.Evaluation evaluation = evaluation(execution, definition, state, items,
                items.isEmpty() ? item(Map.of()) : items.getFirst(), 0);
        Object message = expressions.materialize(config.getOrDefault("message", "Workflow stopped by STOP_AND_ERROR"), evaluation);
        return new IllegalStateException(Objects.toString(message));
    }

    private NodeOutcome executeSubflow(AutomationExecution parent, AutomationFlowDefinition parentDefinition,
                                       Map<String, Object> state, AutomationNode node, Map<String, Object> currentWork,
                                       Map<String, Object> config,
                                       List<Map<String, Object>> items) {
        String flowKey = Objects.toString(config.get("flowKey"), "");
        if (flowKey.isBlank()) throw new IllegalArgumentException("EXECUTE_WORKFLOW requires flowKey");
        Map<String, Object> children = AutomationDataSupport.map(state.get("subflowExecutions"));
        String marker = safeKey(node.id());
        String childId = AutomationDataSupport.string(children.get(marker));
        AutomationExecution child;
        AutomationFlowDefinition childDefinition;
        if (childId == null) {
            String environment = Objects.toString(parent.getContext().getOrDefault("environment", "default"));
            childDefinition = flows.active(parent.getTenantKey(), parent.getSiteKey(), flowKey, environment);
            child = new AutomationExecution();
            child.setExecutionId("exec-" + UUID.randomUUID());
            child.setBlockKey(flowKey);
            child.setAutomationFlowKey(flowKey);
            child.setFlowVersion(childDefinition.getVersion());
            child.setEntryType("SUBFLOW");
            child.setTenantKey(parent.getTenantKey());
            child.setSiteKey(parent.getSiteKey());
            child.setParentExecutionId(parent.getExecutionId());
            child.setExecutionMode(AutomationExecutionMode.ASYNC);
            child.setFailurePolicy(parent.getFailurePolicy());
            child.setInput(Map.of("items", items));
            child.setOutput(new LinkedHashMap<>());
            child.setContext(new LinkedHashMap<>(parent.getContext()));
            child.setInlineFragment(AutomationMapCodec.mongoSafe(objectMapper.convertValue(childDefinition, Map.class)));
            child.setStatus("RUNNING");
            child.setCreatedAt(Instant.now());
            child.setUpdatedAt(Instant.now());
            executions.save(child);
            if (!"N8N_ITEMS".equalsIgnoreCase(childDefinition.getRuntimeMode())) {
                throw new IllegalArgumentException("N8N_ITEMS EXECUTE_WORKFLOW currently requires an N8N_ITEMS child flow");
            }
            run(child, childDefinition);
            child.setUpdatedAt(Instant.now());
            executions.save(child);
            children.put(marker, child.getExecutionId());
            state.put("subflowExecutions", children);
        } else {
            child = executions.findByExecutionId(childId).orElseThrow();
            childDefinition = objectMapper.convertValue(AutomationMapCodec.restore(child.getInlineFragment()), AutomationFlowDefinition.class);
        }
        if (Set.of("RUNNING", "WAITING", "WAITING_CALLBACK", "WAITING_CONCURRENCY").contains(child.getStatus())) {
            queue(state).add(0, new LinkedHashMap<>(currentWork));
            parent.setResumeAt(Instant.now().plusSeconds(1));
            parent.setResumeNodeId("n8n-queue");
            return new NodeOutcome(List.of(), "WAITING", false);
        }
        if (!"COMPLETED".equals(child.getStatus())) throw new IllegalStateException("child workflow ended with status " + child.getStatus());
        children.remove(marker);
        state.put("subflowExecutions", children);
        return NodeOutcome.output("0", items(child.getOutput().get("items")));
    }

    private List<Map<String, Object>> fileMetadata(Map<String, Object> config, List<Map<String, Object>> items) {
        String sourcePath = Objects.toString(config.getOrDefault("sourcePath", "data"));
        String targetPath = Objects.toString(config.getOrDefault("targetPath", sourcePath));
        List<Map<String, Object>> output = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> target = copyItem(item);
            Object binary = AutomationDataSupport.readPath(target.get("binary"), sourcePath);
            AutomationDataSupport.setPath(json(target), targetPath, AutomationDataSupport.fileMetadata(binary));
            output.add(target);
        }
        return output;
    }

    private List<Map<String, Object>> applyCallback(AutomationNode node, List<Map<String, Object>> items, Map<String, Object> payload) {
        Map<String, Object> mappings = AutomationDataSupport.map(node.configOrEmpty().get("callbackMappings"));
        String storePath = Objects.toString(node.configOrEmpty().getOrDefault("callbackStorePath", "callback"));
        List<Map<String, Object>> output = new ArrayList<>();
        for (Map<String, Object> source : items) {
            Map<String, Object> target = copyItem(source);
            if (mappings.isEmpty()) AutomationDataSupport.setPath(json(target), storePath, payload);
            else mappings.forEach((path, sourcePath) -> AutomationDataSupport.setPath(json(target), path,
                    AutomationDataSupport.readPath(payload, Objects.toString(sourcePath))));
            output.add(target);
        }
        return output;
    }

    private void route(AutomationFlowDefinition definition, List<Map<String, Object>> queue, AutomationNode node,
                       List<Emission> emissions, String sourceNodeId) {
        for (Emission emission : emissions) {
            if (emission.port().startsWith("fallback:")) {
                queue.add(work(emission.port().substring("fallback:".length()), "0", emission.items(), sourceNodeId));
                continue;
            }
            List<AutomationEdge> outgoing = definition.getEdges().stream()
                    .filter(edge -> node.id().equals(edge.fromNodeId()))
                    .filter(edge -> matchesPort(edge.fromPort(), emission.port()))
                    .toList();
            for (AutomationEdge edge : outgoing) {
                queue.add(work(edge.toNodeId(), Objects.toString(edge.toPort(), "0"), emission.items(), sourceNodeId));
            }
        }
    }

    private boolean matchesPort(String edgePort, String emissionPort) {
        if (edgePort == null || edgePort.isBlank()) return Set.of("0", "main", "callback").contains(emissionPort);
        return edgePort.equals(emissionPort);
    }

    private void rememberOutputs(Map<String, Object> state, AutomationNode node, List<Emission> emissions) {
        List<Map<String, Object>> combined = emissions.stream().flatMap(emission -> emission.items().stream()).toList();
        List<Object> outputs = new ArrayList<>(AutomationDataSupport.list(state.get("nodeOutputs")));
        outputs.removeIf(entry -> {
            Map<String, Object> map = AutomationDataSupport.map(entry);
            return node.id().equals(map.get("nodeId")) || Objects.equals(node.name(), map.get("nodeName"));
        });
        outputs.add(Map.of("nodeId", node.id(), "nodeName", Objects.toString(node.name(), node.id()), "items", combined));
        state.put("nodeOutputs", outputs);
    }

    private N8nExpressionService.Evaluation evaluation(AutomationExecution execution, AutomationFlowDefinition definition,
                                                        Map<String, Object> state, List<Map<String, Object>> inputItems,
                                                        Map<String, Object> item, int index) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        for (Object raw : AutomationDataSupport.list(state.get("nodeOutputs"))) {
            Map<String, Object> entry = AutomationDataSupport.map(raw);
            outputs.put(Objects.toString(entry.get("nodeId")), entry.get("items"));
            outputs.put(Objects.toString(entry.get("nodeName")), entry.get("items"));
        }
        return new N8nExpressionService.Evaluation(
                item, json(item), AutomationDataSupport.map(item.get("binary")), inputItems, index,
                execution.getInput(),
                Map.of("id", execution.getExecutionId(), "mode", Objects.toString(execution.getExecutionMode()), "resumeUrl", ""),
                Map.of("id", definition.getFlowKey(), "name", Objects.toString(definition.getName(), definition.getFlowKey()), "active", definition.isActive()),
                outputs
        );
    }

    private void applyCredential(AutomationExecution execution, AutomationNode node, HttpHeaders headers) {
        if (node.credentialRef() == null || node.credentialRef().isBlank()) return;
        var credential = credentials.active(execution.getTenantKey(), execution.getSiteKey(), node.credentialRef());
        List<Object> actorRoles = AutomationDataSupport.list(execution.getContext().get("actorRoles"));
        if (!actorRoles.isEmpty() && credential.getAllowedRoles() != null && !credential.getAllowedRoles().isEmpty()
                && credential.getAllowedRoles().stream().noneMatch(actorRoles::contains)) {
            throw new IllegalArgumentException("actor lacks a role required by connector credential");
        }
        String secret = credentials.secret(credential);
        Map<String, Object> metadata = credential.getMetadata();
        String type = Objects.toString(metadata.getOrDefault("authType", credential.getType() == null ? "BEARER" : credential.getType())).toUpperCase(Locale.ROOT);
        switch (type) {
            case "BEARER" -> headers.setBearerAuth(secret);
            case "API_KEY" -> headers.set(Objects.toString(metadata.getOrDefault("headerName", "X-API-KEY")), secret);
            case "BASIC" -> headers.set("Authorization", "Basic " + (secret.contains(":")
                    ? Base64.getEncoder().encodeToString(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)) : secret));
            default -> throw new IllegalArgumentException("unsupported credential authType: " + type);
        }
    }

    private Map<String, AutomationNode> nodeMap(AutomationFlowDefinition definition) {
        Map<String, AutomationNode> result = new LinkedHashMap<>();
        definition.getNodes().forEach(node -> result.put(node.id(), node));
        return result;
    }

    private Map<String, Object> state(AutomationExecution execution) {
        Object existing = execution.getContext().get(STATE_KEY);
        if (existing instanceof Map<?, ?>) return AutomationDataSupport.map(existing);
        Map<String, Object> created = new LinkedHashMap<>();
        execution.getContext().put(STATE_KEY, created);
        return created;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> queue(Map<String, Object> state) {
        Object existing = state.get("queue");
        if (existing instanceof List<?> list) return (List<Map<String, Object>>) list;
        List<Map<String, Object>> created = new ArrayList<>();
        state.put("queue", created);
        return created;
    }

    private List<Map<String, Object>> initialItems(AutomationExecution execution) {
        Object supplied = execution.getInput().get("items");
        if (supplied instanceof Iterable<?>) return items(supplied);
        return List.of(item(execution.getInput()));
    }

    private List<Map<String, Object>> items(Object raw) {
        List<Map<String, Object>> output = new ArrayList<>();
        for (Object value : AutomationDataSupport.list(raw)) {
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> converted = AutomationDataSupport.map(AutomationDataSupport.copy(map));
                output.add(converted.containsKey("json") ? ensureItem(converted) : item(converted));
            } else output.add(item(Map.of("value", value)));
        }
        return output;
    }

    private Map<String, Object> item(Map<String, Object> json) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("json", new LinkedHashMap<>(json));
        return item;
    }

    private Map<String, Object> ensureItem(Map<String, Object> item) {
        item.put("json", AutomationDataSupport.map(item.get("json")));
        if (item.containsKey("binary")) item.put("binary", AutomationDataSupport.map(item.get("binary")));
        return item;
    }

    private Map<String, Object> copyItem(Map<String, Object> source) {
        return ensureItem(AutomationDataSupport.map(AutomationDataSupport.copy(source)));
    }

    private Map<String, Object> json(Map<String, Object> item) {
        Map<String, Object> json = AutomationDataSupport.map(item.get("json"));
        item.put("json", json);
        return json;
    }

    private Map<String, Object> work(String nodeId, String inputPort, List<Map<String, Object>> items, String sourceNode) {
        Map<String, Object> work = new LinkedHashMap<>();
        work.put("nodeId", nodeId);
        work.put("inputPort", inputPort);
        work.put("items", items);
        if (sourceNode != null) work.put("sourceNode", sourceNode);
        return work;
    }

    private List<Map<String, Object>> pinnedItems(AutomationFlowDefinition definition, AutomationNode node, AutomationExecution execution) {
        if (!"MANUAL".equalsIgnoreCase(Objects.toString(execution.getContext().getOrDefault("runMode", "PRODUCTION")))) return null;
        Object pinned = definition.getPinData().getOrDefault(node.id(), definition.getPinData().get(node.name()));
        return pinned == null ? null : items(pinned);
    }

    private AutomationExecutionStep begin(AutomationExecution execution, AutomationNode node, List<Map<String, Object>> items, int attempt) {
        AutomationExecutionStep step = new AutomationExecutionStep();
        step.setNodeId(node.id());
        step.setNodeType(node.type().name());
        step.setAttempt(attempt);
        step.setStatus("RUNNING");
        step.setInputSnapshot(Map.of("items", AutomationDataSupport.copy(items)));
        step.setStartedAt(Instant.now());
        execution.getSteps().add(step);
        return step;
    }

    private void backoff(AutomationNode node, int attempt) {
        long base = node.retryPolicy() == null || node.retryPolicy().backoffMs() == null
                ? 0 : Math.max(0, node.retryPolicy().backoffMs());
        boolean exponential = node.retryPolicy() != null
                && "exponential".equalsIgnoreCase(node.retryPolicy().strategy());
        long delay = exponential ? Math.min(30_000, base * (1L << Math.min(10, attempt - 1))) : base;
        if (delay == 0) return;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("node retry interrupted", interrupted);
        }
    }

    private void finish(AutomationExecutionStep step, String status, List<Emission> emissions, Map<String, Object> error) {
        step.setStatus(status);
        step.setOutputSnapshot(Map.of("outputs", emissions.stream().map(emission -> Map.of("port", emission.port(), "items", emission.items())).toList()));
        step.setErrorSnapshot(error);
        step.setFinishedAt(Instant.now());
    }

    private void persistOutput(AutomationExecution execution, Map<String, Object> state) {
        execution.getContext().put(STATE_KEY, state);
        List<Map<String, Object>> finalItems = items(state.get("finalItems"));
        if (finalItems.isEmpty()) {
            List<Object> outputs = AutomationDataSupport.list(state.get("nodeOutputs"));
            if (!outputs.isEmpty()) finalItems = items(AutomationDataSupport.map(outputs.getLast()).get("items"));
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("items", finalItems);
        if (finalItems.size() == 1) output.put("json", json(finalItems.getFirst()));
        output.put("customData", AutomationDataSupport.map(state.get("customData")));
        if (state.get("webhookResponse") != null) output.put("webhookResponse", state.get("webhookResponse"));
        execution.setOutput(output);
    }

    private void checkpoint(AutomationExecution execution) {
        if (checkpoints != null) checkpoints.checkpoint(execution);
    }

    private String safeKey(String value) { return value.replace(".", "\uFF0E"); }
    private Object normalize(Object value) { return value instanceof Number number ? number.doubleValue() : value; }

    private record Emission(String port, List<Map<String, Object>> items) { }
    private record NodeOutcome(List<Emission> emissions, String waitStatus, boolean callbackWait) {
        static NodeOutcome output(String port, List<Map<String, Object>> items) {
            return new NodeOutcome(List.of(new Emission(port, items)), null, false);
        }
        static NodeOutcome callback(List<Map<String, Object>> items) {
            return new NodeOutcome(List.of(new Emission("callback", items)), "WAITING_CALLBACK", true);
        }
        boolean timedWait() { return "WAITING".equals(waitStatus); }
    }
}
