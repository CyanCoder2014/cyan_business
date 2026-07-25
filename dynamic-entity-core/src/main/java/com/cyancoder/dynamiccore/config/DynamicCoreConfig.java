package com.cyancoder.dynamiccore.config;

import com.cyancoder.dynamiccore.operator.OperatorRegistry;
import com.cyancoder.dynamiccore.operator.operators.CopyFieldOperator;
import com.cyancoder.dynamiccore.operator.operators.SetFieldOperator;
import com.cyancoder.dynamiccore.operator.operators.SumFieldsOperator;
import com.cyancoder.dynamiccore.runtime.DynamicEntityOpenApiService;
import com.cyancoder.dynamiccore.runtime.EndpointDynamicEntityController;
import com.cyancoder.dynamiccore.runtime.EndpointDynamicEntityOpenApiController;
import com.cyancoder.dynamiccore.runtime.InternalDynamicEntityController;
import com.cyancoder.dynamiccore.runtime.InternalDynamicEntityOpenApiController;
import com.cyancoder.dynamiccore.runtime.DynamicEntityResponseMapper;
import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.service.DynamicOperatorEngine;
import com.cyancoder.dynamiccore.service.DynamicValidationEngine;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinitionRepository;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordRepository;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import com.cyancoder.dynamiccore.template.DynamicTemplateRegistry;
import com.cyancoder.dynamiccore.validation.ValidatorRegistry;
import com.cyancoder.dynamiccore.validation.validators.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(DynamicRuntimeProperties.class)
public class DynamicCoreConfig {

    @Bean
    public ValidatorRegistry validatorRegistry() {
        return new ValidatorRegistry(List.of(
                new RequiredValidator(),
                new RegexValidator(),
                new MinLengthValidator(),
                new MaxLengthValidator(),
                new EnumValidator(),
                new DecimalMinValidator(),
                new DecimalMaxValidator(),
                new AntlrExpressionValidator()
        ));
    }

    @Bean
    public OperatorRegistry operatorRegistry() {
        return new OperatorRegistry(List.of(
                new SetFieldOperator(),
                new CopyFieldOperator(),
                new SumFieldsOperator()
        ));
    }

    @Bean
    public DynamicDefinitionParser dynamicDefinitionParser(ObjectMapper objectMapper) {
        return new DynamicDefinitionParser(objectMapper);
    }

    @Bean
    public DynamicEntityResponseMapper dynamicEntityResponseMapper(DynamicDefinitionParser definitionParser) {
        return new DynamicEntityResponseMapper(definitionParser);
    }

    @Bean
    public DynamicValidationEngine dynamicValidationEngine(ValidatorRegistry validatorRegistry, DynamicRuntimeProperties properties) {
        return new DynamicValidationEngine(validatorRegistry, properties.isCheckMissingFields(), properties.isCheckExtraFields());
    }

    @Bean
    public DynamicOperatorEngine dynamicOperatorEngine(OperatorRegistry operatorRegistry) {
        return new DynamicOperatorEngine(operatorRegistry);
    }

    @Bean
    public DynamicTemplateRegistry dynamicTemplateRegistry(List<DynamicTemplateProvider> providers) {
        return new DynamicTemplateRegistry(providers);
    }

    @Bean
    public DynamicRuntimeService dynamicRuntimeService(
            StoredEntityDefinitionRepository definitionRepository,
            DynamicEntityRecordRepository recordRepository,
            DynamicDefinitionParser definitionParser,
            DynamicValidationEngine validationEngine,
            DynamicOperatorEngine operatorEngine,
            DynamicRuntimeProperties properties,
            DynamicTemplateRegistry templateRegistry
    ) {
        return new DynamicRuntimeService(definitionRepository, recordRepository, definitionParser, validationEngine, operatorEngine, properties, templateRegistry);
    }

    @Bean
    public EndpointDynamicEntityController endpointDynamicEntityController(
            DynamicRuntimeService runtimeService,
            DynamicRuntimeProperties properties,
            DynamicEntityResponseMapper responseMapper
    ) {
        return new EndpointDynamicEntityController(runtimeService, properties, responseMapper);
    }

    @Bean
    public InternalDynamicEntityController internalDynamicEntityController(
            DynamicRuntimeService runtimeService,
            DynamicEntityResponseMapper responseMapper
    ) {
        return new InternalDynamicEntityController(runtimeService, responseMapper);
    }

    @Bean
    public DynamicEntityOpenApiService dynamicEntityOpenApiService(
            DynamicRuntimeService runtimeService,
            DynamicDefinitionParser definitionParser,
            DynamicRuntimeProperties properties
    ) {
        return new DynamicEntityOpenApiService(runtimeService, definitionParser, properties);
    }

    @Bean
    public EndpointDynamicEntityOpenApiController endpointDynamicEntityOpenApiController(
            DynamicEntityOpenApiService openApiService,
            DynamicRuntimeProperties properties
    ) {
        return new EndpointDynamicEntityOpenApiController(openApiService, properties);
    }

    @Bean
    public InternalDynamicEntityOpenApiController internalDynamicEntityOpenApiController(
            DynamicEntityOpenApiService openApiService
    ) {
        return new InternalDynamicEntityOpenApiController(openApiService);
    }
}
