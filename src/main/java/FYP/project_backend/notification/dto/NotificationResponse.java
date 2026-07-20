package FYP.project_backend.notification.dto;

import FYP.project_backend.notification.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private NotificationType type;

    private boolean read;

    private LocalDateTime createdAt;

}