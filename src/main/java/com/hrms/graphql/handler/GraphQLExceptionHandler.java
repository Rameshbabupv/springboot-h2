package com.hrms.graphql.handler;

import com.hrms.exception.TemplateConflictException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(
            Throwable ex,
            DataFetchingEnvironment env) {

        if (ex instanceof TemplateConflictException) {
            TemplateConflictException conflict = (TemplateConflictException) ex;

            Map<String, Object> extensions = new HashMap<>();
            extensions.put("conflictType", "TEMPLATE_CRITERIA_CONFLICT");
            extensions.put("conflictCount", conflict.getConflicts().size());
            extensions.put("conflicts", conflict.getConflicts());

            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(extensions)
                .build();
        }

        return super.resolveToSingleError(ex, env);
    }
}
