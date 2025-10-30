package com.shared.entityaudit.listener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.shared.entityaudit.EntityAuditHelper;
import com.shared.entityaudit.descriptor.EntityAuditDescriptor;
import com.shared.entityaudit.model.EntityAuditAction;
import com.shared.entityaudit.model.EntityAuditEventRequest;

/**
 * Spring-managed delegate used by {@link SharedEntityAuditListener} to publish audit events.
 */
public class EntityAuditListenerDelegate {

    private static final Logger log = LoggerFactory.getLogger(EntityAuditListenerDelegate.class);

    private final EntityAuditHelper entityAuditHelper;
    private final EntityManagerFactory entityManagerFactory;

    public EntityAuditListenerDelegate(EntityAuditHelper entityAuditHelper,
                                       EntityManagerFactory entityManagerFactory) {
        this.entityAuditHelper = Objects.requireNonNull(entityAuditHelper, "entityAuditHelper must not be null");
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory must not be null");
    }

    public void handle(EntityAuditDescriptor descriptor,
                       Object entity,
                       EntityAuditAction action,
                       @Nullable Map<String, Object> oldState,
                       @Nullable Map<String, Object> newState) {

        EntityAuditEventRequest request = new EntityAuditEventRequest();
        request.setEntityType(resolveEntityType(descriptor, entity));
        request.setEntityId(descriptor.entityId());
        request.setOperation(action.name());
        request.setOldValues(safeCopy(oldState));
        request.setNewValues(safeCopy(newState));

        Map<String, Object> metadata = new LinkedHashMap<>();
        Map<String, Object> descriptorMetadata = descriptor.auditMetadata();
        if (descriptorMetadata != null && !descriptorMetadata.isEmpty()) {
            metadata.putAll(descriptorMetadata);
        }
        Class<?> entityClass = ClassUtils.getUserClass(entity);
        metadata.putIfAbsent("entityClass", entityClass.getName());
        metadata.putIfAbsent("tableName", resolveTableName(entity));
        if (!metadata.isEmpty()) {
            request.setMetadata(metadata);
        }

        String summary = descriptor.changeSummary(action, request.getOldValues(), request.getNewValues());
        if (StringUtils.hasText(summary)) {
            request.setChangeSummary(summary);
        }

        log.info("Recording entity audit: type={}, id={}, action={}, old={}, new={}",
                request.getEntityType(), request.getEntityId(), action, request.getOldValues(), request.getNewValues());

        entityAuditHelper.recordChange(request);
    }

    private String resolveEntityType(EntityAuditDescriptor descriptor, Object entity) {
        String type = descriptor.entityType();
        if (StringUtils.hasText(type)) {
            return type;
        }
        return ClassUtils.getUserClass(entity).getSimpleName();
    }

    private Map<String, Object> safeCopy(@Nullable Map<String, Object> state) {
        if (state == null || state.isEmpty()) {
            return null;
        }
        return new LinkedHashMap<>(state);
    }

    private String resolveTableName(Object entity) {
        try {
            Class<?> actualClass = ClassUtils.getUserClass(entity);
            var metamodel = entityManagerFactory.getMetamodel();
            EntityType<?> entityType = metamodel.entity(actualClass);
            jakarta.persistence.Table table = actualClass.getAnnotation(jakarta.persistence.Table.class);
            if (table != null && StringUtils.hasText(table.name())) {
                if (StringUtils.hasText(table.schema())) {
                    return table.schema() + "." + table.name();
                }
                return table.name();
            }
            return entityType.getName();
        } catch (IllegalArgumentException ex) {
            return ClassUtils.getUserClass(entity).getSimpleName();
        }
    }
}
