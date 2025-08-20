// src/main/java/com/hashedin/huspark/repository/NotificationLogRepository.java
package com.hashedin.huspark.repository;
import com.hashedin.huspark.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {}
