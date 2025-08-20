// src/main/java/com/hashedin/huspark/entity/AuditLog.java
package com.hashedin.huspark.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "audit_logs",
        indexes = @Index(name = "idx_audit_time", columnList = "created_at")
)
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_email")
    private String actorEmail;      // who did it (nullable for system jobs)

    @Column(name = "event_action")  // avoid reserved word ACTION
    private String action;          // e.g., USER_REGISTER, BOOK_CREATE

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;     // small JSON string

    @Column(name = "is_sensitive")  // avoid reserved word SENSITIVE
    private boolean sensitive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
