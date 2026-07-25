package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.cyancoder.dynamiccore.model.FieldDefinition;
import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiCustomizer;
import com.cyancoder.platformopenapi.PlatformOpenApiProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DynamicEntityOpenApiService {
    private final DynamicRuntimeService runtimeService;
    private final DynamicDefinitionParser definitionParser;
    private final DynamicRuntimeProperties runtimeProperties;

    public DynamicEntityOpenApiService(
            DynamicRuntimeService runtimeService,
            DynamicDefinitionParser definitionParser,
            DynamicRuntimeProperties runtimeProperties
    ) {
        this.runtimeService = runtimeService;
        this.definitionParser = definitionParser;
        this.runtimeProperties = runtimeProperties;
    }

    public OpenAPI generate(
            String entityKey,
            DynamicScope scope,
            PlatformApiSecurity security
    ) {
        StoredEntityDefinition stored = runtimeService.getDefinition(entityKey, scope);
        EntityDefinitionModel definition = definitionParser.parse(stored.getDefinitionJson());
        String schemaPrefix = schemaName(entityKey);
        String dataSchemaName = schemaPrefix + "Data";
        String requestSchemaName = schemaPrefix + "RecordRequest";
        String recordSchemaName = schemaPrefix + "Record";
        String basePath = security == PlatformApiSecurity.BASIC
                ? "/internal/entities"
                : "/endpoint/entities";
        String recordPath = basePath + "/records/" + entityKey;

        Components components = new Components()
                .addSchemas(dataSchemaName, dataSchema(definition))
                .addSchemas(requestSchemaName, requestSchema(dataSchemaName))
                .addSchemas(recordSchemaName, recordSchema(dataSchemaName));

        Paths paths = new Paths()
                .addPathItem(recordPath, collectionPath(requestSchemaName, recordSchemaName))
                .addPathItem(recordPath + "/{recordKey}", itemPath(requestSchemaName, recordSchemaName))
                .addPathItem(recordPath + "/validate", validationPath(dataSchemaName));

        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title(stored.getTitle() == null ? entityKey : stored.getTitle())
                        .description("Runtime OpenAPI for dynamic entity " + entityKey)
                        .version("1.0.0"))
                .components(components)
                .paths(paths);
        openApi.addExtension("x-platform-service-key", runtimeProperties.getServiceKey());
        openApi.addExtension("x-platform-entity-key", entityKey);
        openApi.addExtension("x-platform-entity-type", stored.getEntityType());

        PlatformOpenApiProperties properties = new PlatformOpenApiProperties();
        properties.setDefaultSecurity(security);
        new PlatformOpenApiCustomizer(properties).customise(openApi);
        return openApi;
    }

    private PathItem collectionPath(String requestSchemaName, String recordSchemaName) {
        Operation list = operation("List records", "listDynamicEntityRecords")
                .addParametersItem(query("page", new IntegerSchema()._default(0)))
                .addParametersItem(query("size", new IntegerSchema()._default(200)))
                .addParametersItem(query("sort", new StringSchema()._default("createdAt,desc")))
                .responses(ok(recordListSchema(recordSchemaName)));
        addScopeHeaders(list);

        Operation create = operation("Create or upsert a record", "createDynamicEntityRecord")
                .requestBody(jsonBody(ref(requestSchemaName), true))
                .responses(ok(ref(recordSchemaName)));
        addScopeHeaders(create);
        return new PathItem().get(list).post(create);
    }

    private PathItem itemPath(String requestSchemaName, String recordSchemaName) {
        Parameter recordKey = new Parameter()
                .name("recordKey")
                .in("path")
                .required(true)
                .schema(new StringSchema());
        Operation get = operation("Get a record", "getDynamicEntityRecord")
                .addParametersItem(recordKey)
                .responses(ok(ref(recordSchemaName)));
        addScopeHeaders(get);
        Operation replace = operation("Replace a record", "replaceDynamicEntityRecord")
                .addParametersItem(recordKey)
                .requestBody(jsonBody(ref(requestSchemaName), true))
                .responses(ok(ref(recordSchemaName)));
        addScopeHeaders(replace);
        Operation patch = operation("Patch a record", "patchDynamicEntityRecord")
                .addParametersItem(recordKey)
                .requestBody(jsonBody(ref(requestSchemaName), true))
                .responses(ok(ref(recordSchemaName)));
        addScopeHeaders(patch);
        Operation delete = operation("Delete a record", "deleteDynamicEntityRecord")
                .addParametersItem(recordKey)
                .responses(new ApiResponses().addApiResponse(
                        "200", new ApiResponse().description("Deleted")));
        addScopeHeaders(delete);
        return new PathItem().get(get).put(replace).patch(patch).delete(delete);
    }

    private PathItem validationPath(String dataSchemaName) {
        Operation validate = operation("Validate record data", "validateDynamicEntityRecord")
                .requestBody(jsonBody(ref(dataSchemaName), true))
                .responses(ok(new ObjectSchema()
                        .addProperty("valid", new BooleanSchema())
                        .addProperty("data", ref(dataSchemaName))
                        .addProperty("errors", new ArraySchema().items(new ObjectSchema()))));
        addScopeHeaders(validate);
        return new PathItem().post(validate);
    }

    private Schema<?> dataSchema(EntityDefinitionModel definition) {
        ObjectSchema schema = new ObjectSchema();
        schema.setAdditionalProperties(false);
        if (definition.getFields() == null) {
            return schema;
        }
        List<String> required = new ArrayList<>();
        definition.getFields().forEach((name, field) -> {
            schema.addProperty(name, fieldSchema(field));
            if (isRequired(field)) {
                required.add(name);
            }
        });
        if (!required.isEmpty()) {
            schema.setRequired(required);
        }
        return schema;
    }

    private Schema<?> fieldSchema(FieldDefinition field) {
        String type = field.getType() == null ? "object" : field.getType().toLowerCase(Locale.ROOT);
        Schema<?> schema = switch (type) {
            case "string", "date", "datetime" -> new StringSchema();
            case "number", "decimal" -> new NumberSchema();
            case "integer", "long" -> new IntegerSchema();
            case "boolean" -> new BooleanSchema();
            case "list", "array" -> new ArraySchema().items(itemSchema(field));
            case "object", "map" -> nestedObject(field.getItemValidations());
            default -> new ObjectSchema();
        };
        applyValidations(schema, field.getValidations());
        if (field.getDefaultValue() != null) {
            schema.setDefault(field.getDefaultValue());
        }
        return schema;
    }

    private Schema<?> itemSchema(FieldDefinition field) {
        if (field.getItemValidations() == null || field.getItemValidations().isEmpty()) {
            return new Schema<>();
        }
        return nestedObject(field.getItemValidations());
    }

    private ObjectSchema nestedObject(Map<String, FieldDefinition> fields) {
        ObjectSchema object = new ObjectSchema();
        object.setAdditionalProperties(false);
        if (fields == null) {
            return object;
        }
        List<String> required = new ArrayList<>();
        fields.forEach((name, field) -> {
            object.addProperty(name, fieldSchema(field));
            if (isRequired(field)) {
                required.add(name);
            }
        });
        if (!required.isEmpty()) {
            object.setRequired(required);
        }
        return object;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void applyValidations(Schema<?> schema, List<ValidationRule> validations) {
        if (validations == null) {
            return;
        }
        for (ValidationRule rule : validations) {
            String type = validationType(rule);
            Map<String, Object> params = rule.getValidationParams() == null
                    ? Map.of()
                    : rule.getValidationParams();
            if ("REGEX".equals(type) && params.get("pattern") != null) {
                schema.setPattern(String.valueOf(params.get("pattern")));
            } else if ("MIN_LENGTH".equals(type) && params.get("min") != null) {
                schema.setMinLength(integer(params.get("min")));
            } else if ("MAX_LENGTH".equals(type) && params.get("max") != null) {
                schema.setMaxLength(integer(params.get("max")));
            } else if ("DECIMAL_MIN".equals(type) && params.get("min") != null) {
                schema.setMinimum(decimal(params.get("min")));
            } else if ("DECIMAL_MAX".equals(type) && params.get("max") != null) {
                schema.setMaximum(decimal(params.get("max")));
            } else if ("ENUM".equals(type) && params.get("values") instanceof Collection<?> values) {
                ((Schema) schema).setEnum(new ArrayList<>(values));
            }
        }
    }

    private boolean isRequired(FieldDefinition field) {
        return field.getValidations() != null && field.getValidations().stream()
                .map(this::validationType)
                .anyMatch("REQUIRED"::equals);
    }

    private String validationType(ValidationRule rule) {
        String value = rule.getValidation() == null ? rule.getType() : rule.getValidation();
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private Schema<?> requestSchema(String dataSchemaName) {
        return new ObjectSchema()
                .addProperty("recordKey", new StringSchema())
                .addProperty("tenantKey", new StringSchema())
                .addProperty("siteKey", new StringSchema())
                .addProperty("data", ref(dataSchemaName))
                .required(List.of("data"));
    }

    private Schema<?> recordSchema(String dataSchemaName) {
        return new ObjectSchema()
                .addProperty("id", new StringSchema())
                .addProperty("serviceKey", new StringSchema())
                .addProperty("tenantKey", new StringSchema())
                .addProperty("siteKey", new StringSchema())
                .addProperty("entityKey", new StringSchema())
                .addProperty("recordKey", new StringSchema())
                .addProperty("data", ref(dataSchemaName))
                .addProperty("status", new StringSchema())
                .addProperty("createdAt", new StringSchema().format("date-time"))
                .addProperty("updatedAt", new StringSchema().format("date-time"));
    }

    private Schema<?> arrayPageSchema(String recordSchemaName) {
        return new ObjectSchema()
                .addProperty("content", new ArraySchema().items(ref(recordSchemaName)))
                .addProperty("page", new IntegerSchema())
                .addProperty("size", new IntegerSchema())
                .addProperty("totalElements", new IntegerSchema().format("int64"))
                .addProperty("totalPages", new IntegerSchema());
    }

    private Schema<?> recordListSchema(String recordSchemaName) {
        return new ComposedSchema()
                .oneOf(List.of(
                        new ArraySchema().items(ref(recordSchemaName)),
                        arrayPageSchema(recordSchemaName)))
                .description(
                        "A page envelope when page, size, or sort is supplied; "
                                + "otherwise the legacy record array.");
    }

    private Operation operation(String summary, String operationId) {
        return new Operation().summary(summary).operationId(operationId);
    }

    private void addScopeHeaders(Operation operation) {
        operation.addParametersItem(header("X-Tenant-Key"));
        operation.addParametersItem(header("X-Site-Key"));
    }

    private Parameter header(String name) {
        return new Parameter().name(name).in("header").required(false).schema(new StringSchema());
    }

    private Parameter query(String name, Schema<?> schema) {
        return new Parameter().name(name).in("query").required(false).schema(schema);
    }

    private RequestBody jsonBody(Schema<?> schema, boolean required) {
        return new RequestBody()
                .required(required)
                .content(new Content().addMediaType(
                        "application/json", new MediaType().schema(schema)));
    }

    private ApiResponses ok(Schema<?> schema) {
        return new ApiResponses().addApiResponse(
                "200",
                new ApiResponse()
                        .description("Success")
                        .content(new Content().addMediaType(
                                "application/json", new MediaType().schema(schema))));
    }

    private Schema<?> ref(String name) {
        return new Schema<>().$ref("#/components/schemas/" + name);
    }

    private String schemaName(String entityKey) {
        StringBuilder result = new StringBuilder();
        for (String part : entityKey.split("[^A-Za-z0-9]+")) {
            if (!part.isBlank()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.isEmpty() ? "DynamicEntity" : result.toString();
    }

    private Integer integer(Object value) {
        return Integer.valueOf(String.valueOf(value));
    }

    private BigDecimal decimal(Object value) {
        return new BigDecimal(String.valueOf(value));
    }
}
