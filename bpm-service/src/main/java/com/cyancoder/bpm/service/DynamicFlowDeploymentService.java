package com.cyancoder.bpm.service;

import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.flowable.DynamicFlowBpmnBuilder;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class DynamicFlowDeploymentService {
    private final RepositoryService repositoryService;
    private final DynamicFlowBpmnBuilder bpmnBuilder;

    public DynamicFlowDeploymentService(ObjectProvider<RepositoryService> repositoryServiceProvider,
                                        DynamicFlowBpmnBuilder bpmnBuilder) {
        this.repositoryService = repositoryServiceProvider.getIfAvailable();
        this.bpmnBuilder = bpmnBuilder;
    }

    public Deployment deploy(DynamicFlowDefinition definition) {
        if (repositoryService == null) {
            throw new IllegalStateException("Flowable RepositoryService is not available");
        }
        return repositoryService.createDeployment()
                .name("dynamic-flow-" + definition.getFlowKey() + "-v" + definition.getVersion())
                .key(definition.getFlowKey())
                .addBpmnModel(definition.getFlowKey() + ".bpmn20.xml", bpmnBuilder.build(definition))
                .deploy();
    }

    public boolean isAvailable() {
        return repositoryService != null;
    }
}

