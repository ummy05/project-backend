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


        // =====================================================
        // HIGHLIGHT
        // =====================================================

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
                        background:#fff7ed;
                        border:1px solid #fed7aa;
                        border-left:4px solid #f97316;
                        border-radius:12px;
                        padding:18px;
                        margin:24px 0;
                    ">
    
                        <div style="
                            font-size:13px;
                            color:#6b7280;
                            margin-bottom:6px;
                        ">
                            {{HIGHLIGHT_TITLE}}
                        </div>
    
                        <div style="
                            font-size:22px;
                            font-weight:700;
                            color:#ea580c;
                        ">
                            {{HIGHLIGHT_VALUE}}
                        </div>
    
                    </div>
                    """
                            .replace(
                                    "{{HIGHLIGHT_TITLE}}",
                                    escapeHtml(highlightTitle)
                            )
                            .replace(
                                    "{{HIGHLIGHT_VALUE}}",
                                    escapeHtml(highlightValue)
                            );
        }


        // =====================================================
        // BUTTON
        // =====================================================

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
                    <div style="
                        text-align:center;
                        margin-top:28px;
                    ">
    
                        <a href="{{BUTTON_LINK}}"
                           style="
                                display:inline-block;
                                background:#f97316;
                                color:#ffffff;
                                text-decoration:none;
                                padding:13px 24px;
                                border-radius:8px;
                                font-weight:600;
                           ">
    
                            {{BUTTON_TEXT}}
    
                        </a>
    
                    </div>
                    """
                            .replace(
                                    "{{BUTTON_LINK}}",
                                    escapeHtml(buttonLink)
                            )
                            .replace(
                                    "{{BUTTON_TEXT}}",
                                    escapeHtml(buttonText)
                            );
        }


        // =====================================================
        // MAIN HTML
        // =====================================================

        String html = """

    <!DOCTYPE html>

    <html>

    <head>

        <meta charset="UTF-8">

        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>Coastal Monitor</title>

    </head>


    <body style="
        margin:0;
        padding:0;
        background:#fff7ed;
        font-family:Arial,Helvetica,sans-serif;
    ">


        <div style="
            width:100%;
            max-width:620px;
            margin:40px auto;
            background:#ffffff;
            border-radius:16px;
            overflow:hidden;
            box-shadow:0 4px 20px rgba(0,0,0,0.08);
        ">


            <!-- HEADER -->

            <div style="
                background:#f97316;
                padding:30px 28px;
                text-align:center;
            ">

                <div style="
                    color:#ffffff;
                    font-size:26px;
                    font-weight:700;
                    letter-spacing:0.3px;
                ">

                    Coastal Monitor

                </div>


                <div style="
                    color:#fff7ed;
                    margin-top:7px;
                    font-size:13px;
                    line-height:1.5;
                ">

                    Coastal Conservation and Revenue
                    Monitoring System

                </div>

            </div>


            <!-- ORANGE ACCENT -->

            <div style="
                height:4px;
                background:#ea580c;
                width:100%;
            "></div>


            <!-- CONTENT -->

            <div style="
                padding:35px;
            ">


                <p style="
                    font-size:16px;
                    color:#374151;
                    margin-top:0;
                    margin-bottom:20px;
                ">

                    Dear
                    <strong>{{FULL_NAME}}</strong>,

                </p>


                <h2 style="
                    color:#ea580c;
                    margin-top:20px;
                    margin-bottom:16px;
                    font-size:22px;
                ">

                    {{TITLE}}

                </h2>


                <p style="
                    color:#4b5563;
                    line-height:1.7;
                    font-size:15px;
                    margin-bottom:0;
                ">

                    {{MESSAGE}}

                </p>


                {{HIGHLIGHT}}


                {{BUTTON}}


                <div style="
                    margin-top:35px;
                    padding-top:20px;
                    border-top:1px solid #f3f4f6;
                ">

                    <p style="
                        color:#6b7280;
                        font-size:13px;
                        line-height:1.6;
                        margin:0;
                    ">

                        This is an automated message from
                        <strong style="color:#ea580c;">
                            Coastal Monitor
                        </strong>.

                        Please do not reply to this email.

                    </p>

                </div>


            </div>


            <!-- FOOTER -->

            <div style="
                background:#fff7ed;
                padding:20px;
                text-align:center;
                color:#6b7280;
                font-size:12px;
                border-top:1px solid #fed7aa;
            ">

                <div style="
                    color:#ea580c;
                    font-weight:600;
                    margin-bottom:5px;
                ">

                    Coastal Monitor

                </div>


                © 2026 Coastal Monitor.
                All rights reserved.

            </div>


        </div>


    </body>

    </html>

    """;


        // =====================================================
        // REPLACE SAFE PLACEHOLDERS
        // =====================================================

        html = html.replace(
                "{{FULL_NAME}}",
                escapeHtml(safeName)
        );

        html = html.replace(
                "{{TITLE}}",
                escapeHtml(safeTitle)
        );

        html = html.replace(
                "{{MESSAGE}}",
                safeMessage
        );

        html = html.replace(
                "{{HIGHLIGHT}}",
                highlightHtml
        );

        html = html.replace(
                "{{BUTTON}}",
                buttonHtml
        );


        return html;

    }

    // =====================================================
    // HTML ESCAPE
    // =====================================================

    private String escapeHtml(
            String value
    ) {

        if (value == null) {

            return "";

        }

        return value

                .replace(
                        "&",
                        "&amp;"
                )

                .replace(
                        "<",
                        "&lt;"
                )

                .replace(
                        ">",
                        "&gt;"
                )

                .replace(
                        "\"",
                        "&quot;"
                )

                .replace(
                        "'",
                        "&#39;"
                );

    }

}