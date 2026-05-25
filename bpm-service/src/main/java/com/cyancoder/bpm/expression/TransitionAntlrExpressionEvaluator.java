package com.cyancoder.bpm.expression;

import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.expression.parser.TransitionExpressionLexer;
import com.cyancoder.bpm.expression.parser.TransitionExpressionParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TransitionAntlrExpressionEvaluator {
    public boolean evaluate(String expression,
                            ManagedObject object,
                            Map<String, Object> context,
                            TransitionActorContext actorContext) {
        TransitionExpressionLexer lexer = new TransitionExpressionLexer(CharStreams.fromString(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new TransitionExpressionErrorListener());
        TransitionExpressionParser parser = new TransitionExpressionParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new TransitionExpressionErrorListener());
        Object result = new TransitionExpressionVisitor(buildVariables(object, context, actorContext)).visit(parser.expr());
        return result instanceof Boolean bool && bool;
    }

    private Map<String, Object> buildVariables(ManagedObject object,
                                               Map<String, Object> context,
                                               TransitionActorContext actorContext) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("payload", object == null || object.getPayload() == null ? Map.of() : object.getPayload());
        variables.put("context", context == null ? Map.of() : context);
        variables.put("state", object == null ? null : object.getState());
        variables.put("assignee", object == null ? null : object.getAssignee());
        variables.put("locked", object != null && object.isLocked());
        variables.put("objectType", object == null ? null : object.getObjectType());
        variables.put("flowKey", object == null ? null : object.getFlowKey());
        variables.put("actorUserId", actorContext == null ? null : actorContext.userId());
        variables.put("actorGroups", actorContext == null ? Set.of() : actorContext.groupsOrEmpty());
        variables.put("actorRoles", actorContext == null ? Set.of() : actorContext.rolesOrEmpty());
        return variables;
    }
}

