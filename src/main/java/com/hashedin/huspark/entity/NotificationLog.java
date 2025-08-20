// src/main/java/com/hashedin/huspark/entity/NotificationLog.java
package com.hashedin.huspark.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="notification_logs")
public class NotificationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String recipient;           // we’ll store email here as “phone placeholder”
    private String channel;             // "SMS"
    @Column(length = 1000)
    private String message;
    private LocalDateTime sentAt;
}
