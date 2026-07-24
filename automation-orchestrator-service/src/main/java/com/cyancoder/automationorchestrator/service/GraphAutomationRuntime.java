package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class GraphAutomationRuntime {
    private static final List<String> ACTIVE = List.of("RUNNING","WAITING","WAITING_CALLBACK","WAITING_CONCURRENCY");
    private final InternalServiceHttpSupport http;
    private final ConnectorCredentialService credentials;
    private final AutomationFlowDefinitionService flows;
    private final AutomationExecutionRepository executions;
    private final GoRulesDecisionService decisions;
    private final ObjectMapper objectMapper;
    private final AutomationExecutionCheckpointService checkpoints;
    private final ExpressionParser expressions = new SpelExpressionParser();

    public GraphAutomationRuntime(InternalServiceHttpSupport http, ConnectorCredentialService credentials,
                                  AutomationFlowDefinitionService flows, AutomationExecutionRepository executions,
                                  GoRulesDecisionService decisions) {
        this(http, credentials, flows, executions, decisions, new ObjectMapper(), null);
    }

    @Autowired
    public GraphAutomationRuntime(InternalServiceHttpSupport http, ConnectorCredentialService credentials,
                                  AutomationFlowDefinitionService flows, AutomationExecutionRepository executions,
                                  GoRulesDecisionService decisions, ObjectMapper objectMapper,
                                  AutomationExecutionCheckpointService checkpoints) {
        this.http=http;this.credentials=credentials;this.flows=flows;this.executions=executions;this.decisions=decisions;this.objectMapper=objectMapper;this.checkpoints=checkpoints;
    }

    public void run(AutomationExecution execution, AutomationFlowDefinition definition) {
        Map<String,AutomationNode> nodes=new LinkedHashMap<>();definition.getNodes().forEach(node->nodes.put(node.id(),node));
        String current=execution.getCurrentNodeId()==null?definition.getEntryNodeId():execution.getCurrentNodeId();
        if (Objects.equals(current, definition.getEntryNodeId()) && execution.getSteps().isEmpty()) AutomationSchemaSupport.validate(definition.getInputsSchema(), execution.getOutput(), "automation input");
        int guard=0;
        while(current!=null){
            if(++guard>10000)throw new IllegalStateException("automation exceeded 10000 node executions");
            AutomationNode node=nodes.get(current);if(node==null)throw new IllegalArgumentException("automation node not found: "+current);
            execution.setCurrentNodeId(current);
            if(execution.isCancelRequested()){execution.setStatus("CANCELLED");return;}
            if(!node.isEnabled()){current=next(node.id(),null,definition.getEdges());execution.setCurrentNodeId(current);checkpoint(execution);continue;}
            NodeResult result=runWithPolicies(execution,definition,node);
            if(result.waiting()){execution.setCurrentNodeId(node.id());checkpoint(execution);return;}
            current=result.nextNodeId();execution.setCurrentNodeId(current);execution.setCurrentConcurrencyKey(null);
            if(current!=null)checkpoint(execution);
        }
        execution.setStatus("COMPLETED");execution.setCompletedAt(Instant.now());
        AutomationSchemaSupport.validate(definition.getOutputsSchema(), execution.getOutput(), "automation output");
        checkpoint(execution);
    }

    public void callback(AutomationExecution execution, AutomationFlowDefinition definition, String nodeId, String callbackId, Map<String,Object> payload) {
        @SuppressWarnings("unchecked") List<String> processed=(List<String>)execution.getContext().computeIfAbsent("processedCallbacks",key->new ArrayList<String>());
        String key=callbackId==null||callbackId.isBlank()?"payload:"+Objects.hashCode(payload):callbackId;
        if(processed.contains(key))return;
        if(!"WAITING_CALLBACK".equals(execution.getStatus())||!Objects.equals(nodeId,execution.getCurrentNodeId()))throw new IllegalArgumentException("execution is not waiting at callback node");
        processed.add(key);
        AutomationNode node=definition.getNodes().stream().filter(item->nodeId.equals(item.id())).findFirst().orElseThrow();
        applyMappings(execution.getOutput(),AutomationDataSupport.map(node.configOrEmpty().get("callbackMappings")),payload==null?Map.of():payload);
        String store=AutomationDataSupport.string(node.configOrEmpty().get("callbackStorePath"));if(store!=null)AutomationDataSupport.setPath(execution.getOutput(),store,payload);
        execution.setStatus("RUNNING");execution.setCurrentNodeId(next(nodeId,"callback",definition.getEdges()));run(execution,definition);
    }

    private NodeResult runWithPolicies(AutomationExecution execution,AutomationFlowDefinition definition,AutomationNode node){
        int attempts=node.retryPolicy()==null||node.retryPolicy().maxAttempts()==null?1:Math.max(1,node.retryPolicy().maxAttempts());
        RuntimeException last=null;
        for(int attempt=1;attempt<=attempts;attempt++){
            AutomationExecutionStep step=begin(execution,node,attempt);
            try{
                if(concurrencyExceeded(execution,node)){execution.setStatus("WAITING_CONCURRENCY");execution.setResumeAt(Instant.now().plusSeconds(5));execution.setResumeNodeId(node.id());finish(step,"WAITING_CONCURRENCY",Map.of("resumeAt",execution.getResumeAt().toString()),null);return new NodeResult(null,true);}
                NodeResult result=runNode(execution,definition,node,step);return result;
            }catch(RuntimeException ex){last=ex;finish(step,"FAILED",null,Map.of("message",Objects.toString(ex.getMessage(),"automation node failed")));
                if(attempt<attempts){backoff(node.retryPolicy(),attempt);continue;}
                AutomationErrorPolicy policy=node.errorPolicy();
                if(policy!=null&&Boolean.TRUE.equals(policy.deadLetterOnFailure()))execution.getDeadLetters().add(new LinkedHashMap<>(Map.of("id","dlq-"+UUID.randomUUID(),"nodeId",node.id(),"reason",Objects.toString(ex.getMessage(),"failed"),"createdAt",Instant.now().toString(),"payload",new LinkedHashMap<>(execution.getOutput()))));
                if(policy!=null&&Boolean.TRUE.equals(policy.continueOnFail()))return new NodeResult(next(node.id(),null,definition.getEdges()),false);
                if(policy!=null&&policy.fallbackNodeId()!=null&&!policy.fallbackNodeId().isBlank())return new NodeResult(policy.fallbackNodeId(),false);
                execution.setStatus("FAILED");execution.setError(Map.of("nodeId",node.id(),"message",Objects.toString(ex.getMessage(),"failed")));execution.setCompletedAt(Instant.now());throw ex;
            }
        }
        throw last==null?new IllegalStateException("automation node failed"):last;
    }

    private NodeResult runNode(AutomationExecution ex,AutomationFlowDefinition def,AutomationNode node,AutomationExecutionStep step){
        Map<String,Object> c=node.configOrEmpty();List<AutomationEdge> edges=def.getEdges();
        return switch(node.type()){
            case WEBHOOK_TRIGGER,MANUAL_TRIGGER,SCHEDULE_TRIGGER,ERROR_TRIGGER,MERGE->{finish(step,"COMPLETED",Map.of(),null);yield go(next(node.id(),null,edges));}
            case END->{finish(step,"COMPLETED",Map.of(),null);yield go(null);}
            case WAIT->{Instant at=c.get("resumeAt")==null?Instant.now().plusSeconds(AutomationDataSupport.longValue(value(ex,c.get("delaySeconds")),60)):AutomationDataSupport.instant(value(ex,c.get("resumeAt")));if(at==null)at=Instant.now().plusSeconds(60);ex.setResumeAt(at);ex.setResumeNodeId(next(node.id(),null,edges));ex.setStatus("WAITING");finish(step,"WAITING",Map.of("resumeAt",at.toString()),null);yield waitResult();}
            case WAIT_FOR_CALLBACK->{ex.setResumeNodeId(next(node.id(),"callback",edges));ex.setStatus("WAITING_CALLBACK");finish(step,"WAITING_CALLBACK",Map.of("callbackNodeId",node.id()),null);yield waitResult();}
            case CALL_API,N8N_WORKFLOW->{Map<String,Object> response=call(ex,node,c,node.type()==AutomationNodeType.N8N_WORKFLOW);mapResponse(ex,c,response);finish(step,"COMPLETED",response,null);yield go(next(node.id(),null,edges));}
            case PAGINATED_CALL_API->{Map<String,Object> summary=paginate(ex,node,c);finish(step,"COMPLETED",summary,null);yield go(next(node.id(),null,edges));}
            case RUN_BATCH_JOB->runBatch(ex,node,c,edges,step);
            case IF->{boolean yes=AutomationDataSupport.compare(value(ex,c.get("field")),AutomationDataSupport.string(c.get("operator")),value(ex,c.get("value")));finish(step,"COMPLETED",Map.of("result",yes),null);yield go(next(node.id(),yes?"true":"false",edges));}
            case SWITCH->{Object actual=value(ex,c.get("field"));String port="default";for(var entry:AutomationDataSupport.map(value(ex,c.get("cases"))).entrySet())if(Objects.equals(entry.getValue(),actual)){port=entry.getKey();break;}finish(step,"COMPLETED",Map.of("port",port),null);yield go(next(node.id(),port,edges));}
            case FOR_EACH->{List<Object> source=AutomationDataSupport.list(value(ex,c.get("sourcePath")));int chunk=(int)Math.max(1,AutomationDataSupport.longValue(c.get("chunkSize"),1));List<Object> output=new ArrayList<>();for(int start=0;start<source.size();start+=chunk)for(int i=start;i<Math.min(source.size(),start+chunk);i++){Map<String,Object> local=new LinkedHashMap<>(ex.getContext());local.put("item",source.get(i));local.put("index",i);Object mapped=AutomationDataSupport.materialize(c.get("itemTemplate"),ex.getOutput(),local);output.add(mapped==null?source.get(i):mapped);}String target=Objects.toString(c.getOrDefault("targetPath","forEachResult"));AutomationDataSupport.setPath(ex.getOutput(),target,output);finish(step,"COMPLETED",Map.of("processedCount",output.size(),"targetPath",target),null);yield go(next(node.id(),null,edges));}
            case MAP_FIELDS->{Map<String,Object> changed=new LinkedHashMap<>();for(var entry:AutomationDataSupport.map(c.get("mappings")).entrySet()){Object mapped=value(ex,entry.getValue());AutomationDataSupport.setPath(ex.getOutput(),entry.getKey(),mapped);changed.put(entry.getKey().replace('.', '_'),mapped);}finish(step,"COMPLETED",changed,null);yield go(next(node.id(),null,edges));}
            case JSON_TRANSFORM->{Object transformed=c.containsKey("template")?value(ex,c.get("template")):value(ex,c.get("sourcePath"));String target=Objects.toString(c.getOrDefault("targetPath","transformed"));AutomationDataSupport.setPath(ex.getOutput(),target,transformed);finish(step,"COMPLETED",Map.of("targetPath",target),null);yield go(next(node.id(),null,edges));}
            case FILE_METADATA->{Map<String,Object> metadata=AutomationDataSupport.fileMetadata(value(ex,c.get("sourcePath")));String target=Objects.toString(c.getOrDefault("targetPath",c.get("sourcePath")));AutomationDataSupport.setPath(ex.getOutput(),target,metadata);finish(step,"COMPLETED",metadata,null);yield go(next(node.id(),null,edges));}
            case DEDUP_BY_KEY->{Map<String,Object> result=dedup(ex,c);finish(step,"COMPLETED",result,null);yield go(next(node.id(),null,edges));}
            case CODE->{String expression=AutomationDataSupport.string(c.get("expression"));if(expression==null)throw new IllegalArgumentException("CODE expression is required");SimpleEvaluationContext ctx=SimpleEvaluationContext.forReadOnlyDataBinding().withInstanceMethods().build();ctx.setVariable("variables",ex.getOutput());ctx.setVariable("context",ex.getContext());Object result=expressions.parseExpression(expression).getValue(ctx);String target=Objects.toString(c.getOrDefault("targetPath","codeResult"));AutomationDataSupport.setPath(ex.getOutput(),target,result);Map<String,Object> out=new LinkedHashMap<>();out.put("targetPath",target);out.put("value",result);finish(step,"COMPLETED",out,null);yield go(next(node.id(),null,edges));}
            case JDM_DECISION->{Object input=c.containsKey("inputTemplate")?value(ex,c.get("inputTemplate")):value(ex,c.get("inputPath"));Map<String,Object> result=decisions.evaluate(c,input instanceof Map<?,?>?AutomationDataSupport.map(input):new LinkedHashMap<>(ex.getOutput()));String target=Objects.toString(c.getOrDefault("outputPath","decisionResult"));AutomationDataSupport.setPath(ex.getOutput(),target,result.get("result"));if(c.get("tracePath")!=null)AutomationDataSupport.setPath(ex.getOutput(),c.get("tracePath").toString(),result.get("trace"));if(c.get("performancePath")!=null)AutomationDataSupport.setPath(ex.getOutput(),c.get("performancePath").toString(),result.get("performance"));finish(step,"COMPLETED",result,null);yield go(next(node.id(),null,edges));}
            case SUBFLOW->runSubflow(ex,node,c,edges,step);
            default -> throw new IllegalArgumentException(node.type() + " requires flow runtimeMode=N8N_ITEMS");
        };
    }

    @SuppressWarnings("unchecked")
    private NodeResult runSubflow(AutomationExecution parent, AutomationNode node, Map<String, Object> config,
                                  List<AutomationEdge> edges, AutomationExecutionStep step) {
        String flowKey = AutomationDataSupport.string(value(parent, config.get("flowKey")));
        if (flowKey == null || flowKey.isBlank()) {
            throw new IllegalArgumentException("SUBFLOW flowKey is required");
        }
        Map<String, Object> waiting = AutomationDataSupport.map(parent.getContext().get("subflowExecutions"));
        String marker = node.id().replace(".", "\uFF0E");
        String childExecutionId = AutomationDataSupport.string(waiting.get(marker));
        AutomationExecution child;
        if (childExecutionId == null) {
            String environment = Objects.toString(parent.getContext().getOrDefault("environment", "default"));
            AutomationFlowDefinition childDefinition = flows.active(parent.getTenantKey(), parent.getSiteKey(), flowKey, environment);
            child = new AutomationExecution();
            child.setExecutionId("exec-" + UUID.randomUUID());
            child.setBlockKey(flowKey);
            child.setAutomationFlowKey(flowKey);
            child.setFlowVersion(childDefinition.getVersion());
            child.setEntryType("SUBFLOW");
            child.setExecutionMode(AutomationExecutionMode.ASYNC);
            child.setFailurePolicy(parent.getFailurePolicy());
            child.setTenantKey(parent.getTenantKey());
            child.setSiteKey(parent.getSiteKey());
            child.setParentExecutionId(parent.getExecutionId());
            child.setInput(new LinkedHashMap<>(parent.getOutput()));
            child.setOutput(new LinkedHashMap<>(parent.getOutput()));
            child.setContext(new LinkedHashMap<>(parent.getContext()));
            child.setInlineFragment(AutomationMapCodec.mongoSafe(objectMapper.convertValue(childDefinition, Map.class)));
            child.setCurrentNodeId(childDefinition.getEntryNodeId());
            child.setStatus("RUNNING");
            child.setCreatedAt(Instant.now());
            child.setUpdatedAt(Instant.now());
            executions.save(child);
            run(child, childDefinition);
            child.setUpdatedAt(Instant.now());
            executions.save(child);
            waiting.put(marker, child.getExecutionId());
            parent.getContext().put("subflowExecutions", waiting);
        } else {
            child = executions.findByExecutionId(childExecutionId).orElseThrow();
        }

        if (ACTIVE.contains(child.getStatus())) {
            parent.setStatus("WAITING");
            parent.setResumeAt(Instant.now().plusSeconds(1));
            parent.setResumeNodeId(node.id());
            finish(step, "WAITING_SUBFLOW", Map.of(
                    "childExecutionId", child.getExecutionId(),
                    "childStatus", child.getStatus()
            ), null);
            return waitResult();
        }
        if (!"COMPLETED".equals(child.getStatus())) {
            throw new IllegalStateException("subflow " + flowKey + " ended with status " + child.getStatus());
        }
        AutomationDataSupport.setPath(parent.getOutput(),
                Objects.toString(config.getOrDefault("resultPath", "subflowResult")), child.getOutput());
        waiting.remove(marker);
        if (waiting.isEmpty()) {
            parent.getContext().remove("subflowExecutions");
        } else {
            parent.getContext().put("subflowExecutions", waiting);
        }
        finish(step, "COMPLETED", Map.of(
                "childExecutionId", child.getExecutionId(),
                "status", child.getStatus()
        ), null);
        return go(next(node.id(), null, edges));
    }

    private Map<String,Object> call(AutomationExecution ex,AutomationNode node,Map<String,Object> c,boolean n8n){
        String service=AutomationDataSupport.string(value(ex,c.get("serviceKey")));String path=AutomationDataSupport.string(value(ex,c.get("path")));String url=AutomationDataSupport.string(value(ex,c.get(n8n?"webhookUrl":"url")));HttpMethod method=HttpMethod.valueOf(Objects.toString(value(ex,c.getOrDefault("method","POST"))).toUpperCase(Locale.ROOT));Object body=value(ex,c.get("body"));HttpHeaders headers=new HttpHeaders();AutomationDataSupport.map(value(ex,c.get("headers"))).forEach((k,v)->headers.set(k,Objects.toString(v,"")));applyCredential(ex,node,headers);
        Object raw;if(service!=null&&path!=null){HttpHeaders internal=http.internalHeaders(service,ex.getTenantKey(),ex.getSiteKey());internal.addAll(headers);raw=http.exchange(service,path,method,body,internal,Object.class);}else{if(url==null)throw new IllegalArgumentException((n8n?"N8N_WORKFLOW webhookUrl":"CALL_API url or serviceKey/path")+" is required");raw=http.exchangeUrl(url,method,body,headers,node.timeoutPolicy()==null?null:node.timeoutPolicy().connectTimeoutMs(),node.timeoutPolicy()==null?null:node.timeoutPolicy().readTimeoutMs(),Object.class);}return raw instanceof Map<?,?>?AutomationDataSupport.map(raw):new LinkedHashMap<>(Map.of("data",raw==null?Map.of():raw));
    }

    private Map<String,Object> paginate(AutomationExecution ex,AutomationNode node,Map<String,Object> c){int start=(int)AutomationDataSupport.longValue(value(ex,c.get("pageStart")),0);int end=c.get("pageEnd")!=null?(int)AutomationDataSupport.longValue(value(ex,c.get("pageEnd")),start):start+(int)Math.max(1,AutomationDataSupport.longValue(value(ex,c.get("pageCount")),1))-1;String itemsPath=AutomationDataSupport.string(c.get("itemsPath"));if(itemsPath==null)throw new IllegalArgumentException("itemsPath is required");List<Object> items=new ArrayList<>(),pages=new ArrayList<>();for(int page=start;page<=end;page++){Map<String,Object> local=new LinkedHashMap<>(c);Map<String,Object> body=AutomationDataSupport.map(AutomationDataSupport.copy(value(ex,c.get("body"))));AutomationDataSupport.setPath(body,Objects.toString(c.getOrDefault("pageParamPath","page")),page);if(c.get("size")!=null)AutomationDataSupport.setPath(body,Objects.toString(c.getOrDefault("sizeParamPath","size")),value(ex,c.get("size")));local.put("body",body);Map<String,Object> response=call(ex,node,local,false);pages.add(response);List<Object> found=AutomationDataSupport.list(AutomationDataSupport.readPath(response,itemsPath));items.addAll(found);if(found.isEmpty()&&AutomationDataSupport.bool(c.get("stopOnEmpty"),false))break;}String target=Objects.toString(c.getOrDefault("targetPath","paginatedItems"));AutomationDataSupport.setPath(ex.getOutput(),target,items);if(c.get("pageResponsesPath")!=null)AutomationDataSupport.setPath(ex.getOutput(),c.get("pageResponsesPath").toString(),pages);Map<String,Object> summary=new LinkedHashMap<>();summary.put("items",items);summary.put("pageResponses",pages);summary.put("itemCount",items.size());summary.put("pagesCalled",pages.size());applyMappings(ex.getOutput(),AutomationDataSupport.map(c.get("responseMappings")),summary);return summary;}

    private NodeResult runBatch(AutomationExecution ex, AutomationNode node, Map<String, Object> config,
                                List<AutomationEdge> edges, AutomationExecutionStep step) {
        String definitionKey = AutomationDataSupport.string(value(ex, config.get("definitionKey")));
        if (definitionKey == null || definitionKey.isBlank()) {
            throw new IllegalArgumentException("RUN_BATCH_JOB definitionKey is required");
        }
        Map<String, Object> runs = AutomationDataSupport.map(ex.getContext().get("batchRuns"));
        String marker = node.id().replace(".", "\uFF0E");
        String runId = AutomationDataSupport.string(runs.get(marker));
        HttpHeaders headers = http.internalHeaders("batch-worker-service", ex.getTenantKey(), ex.getSiteKey());
        Map<String, Object> batch;
        if (runId == null) {
            String runKey = AutomationDataSupport.string(value(ex, config.get("runKey")));
            if (runKey == null || runKey.isBlank()) runKey = ex.getExecutionId();
            batch = AutomationDataSupport.map(http.exchange("batch-worker-service",
                    "/internal/batch/definitions/" + definitionKey + "/runs",
                    HttpMethod.POST, Map.of("runKey", runKey), headers, Object.class));
            runId = AutomationDataSupport.string(batch.get("id"));
            if (runId == null) throw new IllegalStateException("batch worker did not return a run id");
            runs.put(marker, runId);
            ex.getContext().put("batchRuns", runs);
        } else {
            batch = AutomationDataSupport.map(http.exchange("batch-worker-service",
                    "/internal/batch/runs/" + runId, HttpMethod.GET, null, headers, Object.class));
        }
        String status = Objects.toString(batch.get("status"), "UNKNOWN");
        if ("QUEUED".equals(status) || "RUNNING".equals(status)) {
            long delay = Math.max(1, AutomationDataSupport.longValue(config.get("pollSeconds"), 15));
            ex.setStatus("WAITING");
            ex.setResumeAt(Instant.now().plusSeconds(delay));
            ex.setResumeNodeId(node.id());
            finish(step, "WAITING_BATCH", batch, null);
            return waitResult();
        }
        if (!"COMPLETED".equals(status)) {
            throw new IllegalStateException("batch run " + runId + " ended with status " + status
                    + ": " + Objects.toString(batch.get("errorMessage"), "unknown failure"));
        }
        String target = Objects.toString(config.getOrDefault("resultPath", "batchResult"));
        AutomationDataSupport.setPath(ex.getOutput(), target, batch);
        runs.remove(marker);
        if (runs.isEmpty()) ex.getContext().remove("batchRuns"); else ex.getContext().put("batchRuns", runs);
        finish(step, "COMPLETED", batch, null);
        return go(next(node.id(), null, edges));
    }
    private Map<String,Object> dedup(AutomationExecution ex,Map<String,Object> c){String source=AutomationDataSupport.string(c.get("sourcePath")),keyPath=AutomationDataSupport.string(c.get("keyPath"));if(source==null||keyPath==null)throw new IllegalArgumentException("DEDUP_BY_KEY requires sourcePath and keyPath");boolean first="FIRST".equalsIgnoreCase(AutomationDataSupport.string(c.getOrDefault("keep","LAST"))),skip=AutomationDataSupport.bool(c.get("skipBlankKeys"),true);Map<String,Object> indexed=new LinkedHashMap<>();List<Object> list=AutomationDataSupport.list(value(ex,source));for(Object item:list){Object raw=AutomationDataSupport.readPath(item,keyPath);String key=raw==null?"":raw.toString();if(key.isBlank()&&skip)continue;if(first&&indexed.containsKey(key))continue;indexed.put(key,item);}List<Object> output=new ArrayList<>(indexed.values());String target=Objects.toString(c.getOrDefault("targetPath","dedupedItems"));AutomationDataSupport.setPath(ex.getOutput(),target,output);return Map.of("sourceCount",list.size(),"uniqueCount",output.size(),"targetPath",target);}
    private void mapResponse(AutomationExecution ex,Map<String,Object> c,Map<String,Object> response){Map<String,Object> mappings=AutomationDataSupport.map(c.get("responseMappings"));if(!mappings.isEmpty())applyMappings(ex.getOutput(),mappings,response);else if(c.get("storeResponseAt")==null)ex.setOutput(new LinkedHashMap<>(response));if(c.get("storeResponseAt")!=null)AutomationDataSupport.setPath(ex.getOutput(),c.get("storeResponseAt").toString(),response);}
    private void applyMappings(Map<String,Object> target,Map<String,Object> mappings,Map<String,Object> source){mappings.forEach((path,sourcePath)->AutomationDataSupport.setPath(target,path,AutomationDataSupport.readPath(source,Objects.toString(sourcePath))));}
    private void applyCredential(AutomationExecution ex,AutomationNode node,HttpHeaders headers){if(node.credentialRef()==null||node.credentialRef().isBlank())return;ConnectorCredential credential=credentials.active(ex.getTenantKey(),ex.getSiteKey(),node.credentialRef());List<Object> actorRoles=AutomationDataSupport.list(ex.getContext().get("actorRoles"));if(!actorRoles.isEmpty()&&credential.getAllowedRoles()!=null&&!credential.getAllowedRoles().isEmpty()&&credential.getAllowedRoles().stream().noneMatch(actorRoles::contains))throw new IllegalArgumentException("actor lacks a role required by connector credential");String secret=credentials.secret(credential);Map<String,Object> meta=credential.getMetadata();String type=Objects.toString(meta.getOrDefault("authType",credential.getType()==null?"BEARER":credential.getType())).toUpperCase(Locale.ROOT);switch(type){case"BEARER"->headers.setBearerAuth(secret);case"API_KEY"->headers.set(Objects.toString(meta.getOrDefault("headerName","X-API-KEY")),secret);case"BASIC"->{String encoded=secret.contains(":")?Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8)):secret;headers.set("Authorization","Basic "+encoded);}default->throw new IllegalArgumentException("unsupported credential authType: "+type);}AutomationDataSupport.map(meta.get("headers")).forEach((k,v)->headers.putIfAbsent(k,List.of(Objects.toString(v,""))));}
    private boolean concurrencyExceeded(AutomationExecution ex,AutomationNode node){if(node.concurrencyPolicy()==null||node.concurrencyPolicy().maxConcurrency()==null)return false;String key=Objects.toString(value(ex,node.concurrencyPolicy().keyExpression()),"default");ex.setCurrentConcurrencyKey(key);long running=executions.countByStatusInAndCurrentNodeIdAndCurrentConcurrencyKey(ACTIVE,node.id(),key);Optional<AutomationExecution> persisted=executions.findByExecutionId(ex.getExecutionId());if(persisted!=null&&persisted.filter(item->ACTIVE.contains(item.getStatus())&&node.id().equals(item.getCurrentNodeId())&&key.equals(item.getCurrentConcurrencyKey())).isPresent())running--;return running>=Math.max(1,node.concurrencyPolicy().maxConcurrency());}
    private AutomationExecutionStep begin(AutomationExecution ex,AutomationNode node,int attempt){AutomationExecutionStep step=new AutomationExecutionStep();step.setNodeId(node.id());step.setNodeType(node.type().name());step.setAttempt(attempt);step.setStatus("RUNNING");step.setInputSnapshot(AutomationDataSupport.map(AutomationDataSupport.copy(ex.getOutput())));step.setStartedAt(Instant.now());ex.getSteps().add(step);return step;}
    private void finish(AutomationExecutionStep step,String status,Map<String,Object> output,Map<String,Object> error){step.setStatus(status);step.setOutputSnapshot(output);step.setErrorSnapshot(error);step.setFinishedAt(Instant.now());}
    private void backoff(AutomationRetryPolicy policy,int attempt){long base=policy==null||policy.backoffMs()==null?0:Math.max(0,policy.backoffMs());long delay=policy!=null&&"exponential".equalsIgnoreCase(policy.strategy())?base*(1L<<Math.min(attempt-1,10)):base;if(delay>0)try{Thread.sleep(delay);}catch(InterruptedException ex){Thread.currentThread().interrupt();}}
    private Object value(AutomationExecution ex,Object value){return AutomationDataSupport.resolve(value,ex.getOutput(),ex.getContext());}
    private String next(String node,String port,List<AutomationEdge> edges){return (edges==null?List.<AutomationEdge>of():edges).stream().filter(e->node.equals(e.fromNodeId())).filter(e->port==null?e.fromPort()==null||e.fromPort().isBlank():port.equals(e.fromPort())).map(AutomationEdge::toNodeId).findFirst().orElse(null);}
    private NodeResult go(String next){return new NodeResult(next,false);}private NodeResult waitResult(){return new NodeResult(null,true);}private record NodeResult(String nextNodeId,boolean waiting){}
    private void checkpoint(AutomationExecution execution){if(checkpoints!=null)checkpoints.checkpoint(execution);}
}
