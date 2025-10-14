package com.shared.audit.repository;

import com.shared.audit.entity.AuditEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;

@Repository
public class AuditEventRepository {

    private final JpaRepository<AuditEvent, Long> delegate;

    public AuditEventRepository(@Qualifier("auditEntityManagerFactory") EntityManager entityManager) {
        this.delegate = new JpaRepositoryFactory(entityManager).getRepository(AuditEventRepositoryInternal.class);
    }

    // Delegate methods
    public AuditEvent save(AuditEvent event) {
        return delegate.save(event);
    }

    // Add other methods as needed, delegating to 'delegate'

    // Internal interface for JpaRepository
    private interface AuditEventRepositoryInternal extends JpaRepository<AuditEvent, Long> {}
}
