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

    ) {

        if (user == null) {

            System.err.println(
                    "NOTIFICATION FAILED: user is null."
            );

            return;
        }


        // =================================================
        // SAVE IN-APP NOTIFICATION
        // =================================================

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


        // =================================================
        // SEND EMAIL
        // =================================================

        try {

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


            System.out.println(
                    "Notification email sent to: "
                            + user.getEmail()
            );

        }
        catch (Exception ex) {

            /*
             * IMPORTANT:
             *
             * In-app notification remains saved even
             * if email delivery fails.
             */

            System.err.println(
                    "Notification email failed for: "
                            + user.getEmail()
            );

            ex.printStackTrace();

        }

    }

}