package com.shared.entityaudit.listener;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread-local storage for retaining entity state snapshots between JPA lifecycle callbacks.
 */
final class EntityAuditSnapshotHolder {

    private static final ThreadLocal<Map<Object, Map<String, Object>>> SNAPSHOTS =
            ThreadLocal.withInitial(IdentityHashMap::new);

    private EntityAuditSnapshotHolder() {
    }

    static Map<String, Object> capture(Object entity, Map<String, Object> state) {
        Map<Object, Map<String, Object>> snapshots = SNAPSHOTS.get();
        Map<String, Object> copy = defensiveCopy(state);
        snapshots.put(entity, copy);
        return copy;
    }

    static Map<String, Object> get(Object entity) {
        Map<Object, Map<String, Object>> snapshots = SNAPSHOTS.get();
        Map<String, Object> stored = snapshots.get(entity);
        return stored != null ? stored : Map.of();
    }

    static Map<String, Object> remove(Object entity) {
        Map<Object, Map<String, Object>> snapshots = SNAPSHOTS.get();
        Map<String, Object> removed = snapshots.remove(entity);
        return removed != null ? removed : Map.of();
    }

    static void clearIfEmpty(Object entity) {
        Map<Object, Map<String, Object>> snapshots = SNAPSHOTS.get();
        snapshots.remove(entity);
        if (snapshots.isEmpty()) {
            SNAPSHOTS.remove();
        }
    }

    private static Map<String, Object> defensiveCopy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return new LinkedHashMap<>(source);
    }
}

