package com.astrokiddo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer pageableParameterCustomizer() {
        return openApi -> {
            if (openApi == null) {
                return;
            }

            Map<String, PathItem> paths = openApi.getPaths();
            if (paths != null) {
                paths.values().stream()
                        .filter(Objects::nonNull)
                        .forEach(this::customizePathItem);
            }

            removePageableSchema(openApi);
        };
    }

    private void customizePathItem(PathItem pathItem) {
        pathItem.readOperations().forEach(this::customizeOperation);
    }

    private void customizeOperation(Operation operation) {
        List<Parameter> parameters = operation.getParameters();
        if (parameters == null) {
            return;
        }

        List<Parameter> updatedParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (isPageableQueryParameter(parameter)) {
                updatedParameters.add(createPageParameter());
                updatedParameters.add(createSizeParameter());
                updatedParameters.add(createSortParameter());
            } else {
                updatedParameters.add(parameter);
            }
        }

        operation.setParameters(updatedParameters);
    }

    private boolean isPageableQueryParameter(Parameter parameter) {
        if (parameter == null || parameter.getSchema() == null) {
            return false;
        }

        Schema<?> schema = parameter.getSchema();
        return "query".equals(parameter.getIn())
                && "#/components/schemas/Pageable".equals(schema.get$ref());
    }

    private Parameter createPageParameter() {
        IntegerSchema schema = new IntegerSchema();
        schema.setDefault(0);

        Parameter parameter = new Parameter();
        parameter.setName("page");
        parameter.setIn("query");
        parameter.setSchema(schema);
        parameter.setRequired(false);
        return parameter;
    }

    private Parameter createSizeParameter() {
        IntegerSchema schema = new IntegerSchema();
        schema.setDefault(20);

        Parameter parameter = new Parameter();
        parameter.setName("size");
        parameter.setIn("query");
        parameter.setSchema(schema);
        parameter.setRequired(false);
        return parameter;
    }

    private Parameter createSortParameter() {
        StringSchema schema = new StringSchema();

        Parameter parameter = new Parameter();
        parameter.setName("sort");
        parameter.setIn("query");
        parameter.setSchema(schema);
        parameter.setRequired(false);
        return parameter;
    }

    private void removePageableSchema(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null || components.getSchemas() == null) {
            return;
        }

        components.getSchemas().remove("Pageable");
    }
}