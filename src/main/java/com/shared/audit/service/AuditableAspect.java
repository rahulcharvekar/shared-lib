package com.shared.audit.service;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.shared.common.annotation.Auditable;
import com.shared.utilities.AuditHelper;

/**
 * Aspect that intercepts methods annotated with @Auditable and records audit events.
 */
@Aspect
@Component
public class AuditableAspect {

    private final AuditHelper auditHelper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public AuditableAspect(AuditHelper auditHelper) {
        this.auditHelper = auditHelper;
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = null;
        String outcome = "SUCCESS";
        Map<String, Object> details = new HashMap<>();

        try {
            result = joinPoint.proceed();

            // Add result to details if needed
            if (result != null) {
                details.put("result", result);
            }

        } catch (Throwable throwable) {
            outcome = "FAILURE";
            details.put("error", throwable.getMessage());
            throw throwable;
        } finally {
            // Evaluate SpEL expressions for resourceId and details
            String resourceId = evaluateSpel(auditable.resourceId(), joinPoint);
            Map<String, Object> additionalDetails = evaluateDetailsSpel(auditable.details(), joinPoint);
            Map<String, Object> oldValues = evaluateDetailsSpel(auditable.oldValues(), joinPoint);
            Map<String, Object> newValues = evaluateDetailsSpel(auditable.newValues(), joinPoint);

            if (additionalDetails != null) {
                details.putAll(additionalDetails);
            }

            auditHelper.recordAudit(
                auditable.action(),
                auditable.resourceType(),
                resourceId,
                outcome,
                details,
                oldValues,
                newValues
            );
        }

        return result;
    }

    private String evaluateSpel(String spelExpression, ProceedingJoinPoint joinPoint) {
        if (spelExpression == null || spelExpression.isBlank()) {
            return null;
        }

        try {
            Expression expression = expressionParser.parseExpression(spelExpression);
            StandardEvaluationContext context = createEvaluationContext(joinPoint);
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            // If SpEL evaluation fails, return the expression as is or log error
            return spelExpression;
        }
    }

    private Map<String, Object> evaluateDetailsSpel(String spelExpression, ProceedingJoinPoint joinPoint) {
        if (spelExpression == null || spelExpression.isBlank()) {
            return null;
        }

        try {
            Expression expression = expressionParser.parseExpression(spelExpression);
            StandardEvaluationContext context = createEvaluationContext(joinPoint);
            @SuppressWarnings("unchecked")
            Map<String, Object> details = expression.getValue(context, Map.class);
            return details;
        } catch (Exception e) {
            return null;
        }
    }

    private StandardEvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // Add method name and class
        context.setVariable("methodName", signature.getName());
        context.setVariable("className", signature.getDeclaringType().getSimpleName());

        return context;
    }
}
