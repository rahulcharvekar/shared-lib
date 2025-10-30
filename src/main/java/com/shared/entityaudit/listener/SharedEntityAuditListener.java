package com.shared.entityaudit.listener;

import java.util.Map;
import java.util.Objects;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import com.shared.entityaudit.descriptor.EntityAuditDescriptor;
import com.shared.entityaudit.model.EntityAuditAction;

/**
 * JPA lifecycle listener that forwards audited entity changes to the shared
 * {@link EntityAuditHelper}.
 */
public class SharedEntityAuditListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SharedEntityAuditListener.class);
    private static volatile EntityAuditListenerDelegate delegate;

    public static void setDelegate(EntityAuditListenerDelegate auditDelegate) {
        delegate = Objects.requireNonNull(auditDelegate, "delegate must not be null");
    }

    @PostLoad
    public void postLoad(Object entity) {
        if (entity instanceof EntityAuditDescriptor descriptor) {
            if (log.isDebugEnabled()) {
                log.debug("EntityAudit postLoad for {}", entity.getClass().getName());
            }
            EntityAuditSnapshotHolder.capture(entity, descriptor.auditState());
        }
    }

    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof EntityAuditDescriptor descriptor) {
            Map<String, Object> state = descriptor.auditState();
            EntityAuditSnapshotHolder.capture(entity, state);
            log.info("EntityAudit postPersist for {} id={}", entity.getClass().getName(), descriptor.entityId());
            publish(descriptor, entity, EntityAuditAction.CREATE, null, state);
        }
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof EntityAuditDescriptor descriptor) {
            Map<String, Object> previous = EntityAuditSnapshotHolder.get(entity);
            Map<String, Object> current = descriptor.auditState();
            EntityAuditSnapshotHolder.capture(entity, current);
            log.info("EntityAudit postUpdate for {} id={}", entity.getClass().getName(), descriptor.entityId());
            publish(descriptor, entity, EntityAuditAction.UPDATE, previous, current);
        }
    }

    @PostRemove
    public void postRemove(Object entity) {
        if (entity instanceof EntityAuditDescriptor descriptor) {
            Map<String, Object> previous = EntityAuditSnapshotHolder.remove(entity);
            log.info("EntityAudit postRemove for {} id={}", entity.getClass().getName(), descriptor.entityId());
            publish(descriptor, entity, EntityAuditAction.DELETE, previous.isEmpty() ? descriptor.auditState() : previous, null);
            EntityAuditSnapshotHolder.clearIfEmpty(entity);
        }
    }

    private void publish(EntityAuditDescriptor descriptor,
                         Object entity,
                         EntityAuditAction action,
                         Map<String, Object> oldState,
                         Map<String, Object> newState) {
        EntityAuditListenerDelegate current = delegate;
        if (current == null) {
            return;
        }
        current.handle(descriptor, entity, action, oldState, newState);
    }
}
