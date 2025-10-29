package com.shared.entityaudit.service;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.shared.entityaudit.EntityAuditHelper;
import com.shared.entityaudit.annotation.EntityAuditable;

/**
 * Aspect that intercepts methods annotated with {@link EntityAuditable}.
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "shared-lib.entity-audit", name = "enabled", havingValue = "true")
public class EntityAuditableAspect {

    private final EntityAuditHelper entityAuditHelper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public EntityAuditableAspect(EntityAuditHelper entityAuditHelper) {
        this.entityAuditHelper = entityAuditHelper;
    }

    @Around("@annotation(entityAuditable)")
    public Object auditEntityChange(ProceedingJoinPoint joinPoint, EntityAuditable entityAuditable) throws Throwable {
        StandardEvaluationContext preContext = createEvaluationContext(joinPoint, null);
        Map<String, Object> oldValues = evaluateMap(entityAuditable.oldValues(), preContext);
        Map<String, Object> metadata = evaluateMap(entityAuditable.metadata(), preContext);
        String recordNumber = evaluateString(entityAuditable.recordNumber(), preContext);
        String entityId = evaluateString(entityAuditable.entityId(), preContext);
        String auditNumber = evaluateString(entityAuditable.auditNumber(), preContext);

        Object result = joinPoint.proceed();

        StandardEvaluationContext postContext = createEvaluationContext(joinPoint, result);
        Map<String, Object> newValues = evaluateMap(entityAuditable.newValues(), postContext);
        Map<String, Object> metadataAfter = evaluateMap(entityAuditable.metadata(), postContext);
        if (metadataAfter != null) {
            if (metadata == null) {
                metadata = metadataAfter;
            } else {
                metadata.putAll(metadataAfter);
            }
        }
        if (!StringUtils.hasText(recordNumber)) {
            recordNumber = evaluateString(entityAuditable.recordNumber(), postContext);
        }
        if (!StringUtils.hasText(entityId)) {
            entityId = evaluateString(entityAuditable.entityId(), postContext);
        }
        if (!StringUtils.hasText(auditNumber)) {
            auditNumber = evaluateString(entityAuditable.auditNumber(), postContext);
        }
        String changeSummary = evaluateString(entityAuditable.changeSummary(), postContext);

        entityAuditHelper.recordChange(
                entityAuditable.entityType(),
                recordNumber,
                entityId,
                entityAuditable.operation(),
                oldValues,
                newValues,
                metadata,
                changeSummary,
                auditNumber);

        return result;
    }

    private StandardEvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        if (result != null) {
            context.setVariable("result", result);
        }
        context.setVariable("methodName", signature.getName());
        context.setVariable("className", signature.getDeclaringType().getSimpleName());
        return context;
    }

    private String evaluateString(String expressionValue, StandardEvaluationContext context) {
        if (!StringUtils.hasText(expressionValue)) {
            return null;
        }
        if (!expressionValue.contains("#")) {
            return expressionValue;
        }
        try {
            Expression expression = expressionParser.parseExpression(expressionValue);
            return expression.getValue(context, String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> evaluateMap(String expressionValue, StandardEvaluationContext context) {
        if (!StringUtils.hasText(expressionValue)) {
            return null;
        }
        try {
            Expression expression = expressionParser.parseExpression(expressionValue);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = expression.getValue(context, Map.class);
            if (result == null) {
                return null;
            }
            return new HashMap<>(result);
        } catch (Exception ex) {
            return null;
        }
    }
}
