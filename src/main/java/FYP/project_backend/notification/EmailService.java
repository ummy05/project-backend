package FYP.project_backend.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    // =====================================================
    // SEND NOTIFICATION EMAIL
    // =====================================================

    public void sendNotificationEmail(

            String to,

            String subject,

            String fullName,

            String title,

            String message,

            String highlightTitle,

            String highlightValue,

            String buttonText,

            String buttonLink

    ) {

        if (to == null || to.isBlank()) {

            System.out.println(
                    "EMAIL NOT SENT: recipient email is empty."
            );

            return;
        }


        try {

            MimeMessage mimeMessage =
                    mailSender.createMimeMessage();


            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            true,
                            "UTF-8"
                    );


            helper.setFrom(fromEmail);

            helper.setTo(to);

            helper.setSubject(subject);


            helper.setText(

                    buildNotificationHtml(

                            fullName,

                            title,

                            message,

                            highlightTitle,

                            highlightValue,

                            buttonText,

                            buttonLink

                    ),

                    true
            );


            mailSender.send(mimeMessage);


            System.out.println(
                    "EMAIL SENT SUCCESSFULLY TO: " + to
            );

        }
        catch (MessagingException ex) {

            System.err.println(
                    "EMAIL FAILED TO: " + to
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Failed to send notification email.",
                    ex
            );

        }
        catch (Exception ex) {

            System.err.println(
                    "UNEXPECTED EMAIL ERROR TO: " + to
            );

            ex.printStackTrace();

            throw new RuntimeException(
                    "Failed to send notification email.",
                    ex
            );
        }

    }


    // =====================================================
    // BUILD HTML
    // =====================================================

    private String buildNotificationHtml(

            String fullName,

            String title,

            String message,

            String highlightTitle,

            String highlightValue,

            String buttonText,

            String buttonLink

    ) {

        String safeName =
                fullName == null
                        ? "Customer"
                        : fullName;

        String safeTitle =
                title == null
                        ? "Coastal Monitor Notification"
                        : title;

        String safeMessage =
                message == null
                        ? ""
                        : message;

        String highlightHtml = "";

        if (
                highlightTitle != null
                        &&
                        !highlightTitle.isBlank()
                        &&
                        highlightValue != null
                        &&
                        !highlightValue.isBlank()
        ) {

            highlightHtml =

                    """
                    <div style="
                        background:#f4f7f8;
                        border:1px solid #e0e6e8;
                        border-radius:12px;
                        padding:18px;
                        margin:24px 0;
                    ">

                        <div style="
                            font-size:13px;
                            color:#6b7280;
                            margin-bottom:6px;
                        ">
                            %s
                        </div>

                        <div style="
                            font-size:22px;
                            font-weight:700;
                            color:#0b4f4a;
                        ">
                            %s
                        </div>

                    </div>
                    """
                            .formatted(
                                    escapeHtml(highlightTitle),
                                    escapeHtml(highlightValue)
                            );
        }


        String buttonHtml = "";

        if (
                buttonText != null
                        &&
                        !buttonText.isBlank()
                        &&
                        buttonLink != null
                        &&
                        !buttonLink.isBlank()
        ) {

            buttonHtml =

                    """
                    <div style="text-align:center;margin-top:28px;">

                        <a href="%s"
                           style="
                                display:inline-block;
                                background:#0b4f4a;
                                color:#ffffff;
                                text-decoration:none;
                                padding:13px 24px;
                                border-radius:8px;
                                font-weight:600;
                           ">

                            %s

                        </a>

                    </div>
                    """
                            .formatted(
                                    escapeHtml(buttonLink),
                                    escapeHtml(buttonText)
                            );
        }


        return """

        <!DOCTYPE html>

        <html>

        <head>

            <meta charset="UTF-8">

        </head>


        <body style="
            margin:0;
            padding:0;
            background:#f5f7f8;
            font-family:Arial,Helvetica,sans-serif;
        ">


            <div style="
                max-width:620px;
                margin:40px auto;
                background:#ffffff;
                border-radius:16px;
                overflow:hidden;
                box-shadow:0 4px 20px rgba(0,0,0,0.08);
            ">


                <!-- HEADER -->

                <div style="
                    background:#0b4f4a;
                    padding:28px;
                    text-align:center;
                ">

                    <div style="
                        color:#ffffff;
                        font-size:24px;
                        font-weight:700;
                    ">

                        Coastal Monitor

                    </div>

                    <div style="
                        color:#d4af37;
                        margin-top:6px;
                        font-size:13px;
                    ">

                        ICT-Based Coastal Conservation &
                        Revenue Monitoring System

                    </div>

                </div>


                <!-- CONTENT -->

                <div style="padding:35px;">


                    <p style="
                        font-size:16px;
                        color:#374151;
                    ">

                        Dear
                        <strong>%s</strong>,

                    </p>


                    <h2 style="
                        color:#0b4f4a;
                        margin-top:20px;
                    ">

                        %s

                    </h2>


                    <p style="
                        color:#4b5563;
                        line-height:1.7;
                        font-size:15px;
                    ">

                        %s

                    </p>


                    %s


                    %s


                    <p style="
                        color:#6b7280;
                        font-size:13px;
                        margin-top:35px;
                    ">

                        This is an automated message from
                        Coastal Monitor.

                        Please do not reply to this email.

                    </p>


                </div>


                <!-- FOOTER -->

                <div style="
                    background:#f4f7f8;
                    padding:20px;
                    text-align:center;
                    color:#6b7280;
                    font-size:12px;
                ">

                    © 2026 Coastal Monitor.
                    All rights reserved.

                </div>


            </div>


        </body>

        </html>

        """
                .formatted(

                        escapeHtml(safeName),

                        escapeHtml(safeTitle),

                        safeMessage,

                        highlightHtml,

                        buttonHtml
                );

    }


    // =====================================================
    // HTML ESCAPE
    // =====================================================

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value

                .replace("&", "&amp;")

                .replace("<", "&lt;")

                .replace(">", "&gt;")

                .replace("\"", "&quot;")

                .replace("'", "&#39;");

    }

}