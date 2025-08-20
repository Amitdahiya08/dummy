// src/main/java/com/hashedin/huspark/repository/AuditLogRepository.java
package com.hashedin.huspark.repository;
import com.hashedin.huspark.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
