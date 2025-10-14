package com.shared.utilities.logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerFactoryProvider {

    private static final ConcurrentMap<Class<?>, Logger> CACHE = new ConcurrentHashMap<>();

    private LoggerFactoryProvider() {
    }

    public static Logger getLogger(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        return CACHE.computeIfAbsent(type, LoggerFactory::getLogger);
    }

    public static Logger getLogger(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Logger name must not be blank");
        }
        return LoggerFactory.getLogger(name);
    }
}
