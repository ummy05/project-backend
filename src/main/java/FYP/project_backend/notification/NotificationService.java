package FYP.project_backend.notification;

import FYP.project_backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    private final EmailService emailService;

    public void notify(

            User user,

            String subject,

            String title,

            String message,

            FYP.project_backend.notification.NotificationType type,

            String highlightTitle,

            String highlightValue,

            String buttonText,

            String buttonLink

    ){

        Notification notification =

                Notification.builder()

                        .title(title)

                        .message(message)

                        .type(type)

                        .isRead(false)

                        .createdAt(LocalDateTime.now())

                        .user(user)

                        .build();

        repository.save(notification);

        try{

            emailService.sendNotificationEmail(

                    user.getEmail(),

                    subject,

                    user.getFullName(),

                    title,

                    message,

                    highlightTitle,

                    highlightValue,

                    buttonText,

                    buttonLink

            );

        }

        catch (Exception ex){

            ex.printStackTrace();

        }

    }

}