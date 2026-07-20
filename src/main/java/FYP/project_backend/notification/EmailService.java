package FYP.project_backend.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

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

    ){

        try{

            MimeMessage mimeMessage =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(

                            mimeMessage,

                            true,

                            "UTF-8"

                    );

            helper.setTo(to);

            helper.setSubject(subject);

            helper.setFrom(
                    "YOUR_EMAIL@gmail.com",
                    "ICT-Based Coastal Conservation System"
            );

            helper.setText("""

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

</head>

<body style="

margin:0;

padding:40px;

background:#FAF8F6;

font-family:Segoe UI,Arial,sans-serif;

">

<div style="

max-width:720px;

margin:auto;

background:#FFFFFF;

border-radius:24px;

overflow:hidden;

box-shadow:0 15px 45px rgba(0,0,0,.08);

">

<div style="

background:linear-gradient(135deg,#4B2E83,#6C4AB6);

padding:35px;

text-align:center;

color:white;

">

<h1 style="

margin:0;

font-size:28px;

">

ICT-Based Coastal Conservation

</h1>

<p style="

margin-top:8px;

opacity:.9;

">

& Revenue Monitoring System

</p>

</div>

<div style="padding:45px;">

<h2 style="

margin-top:0;

color:#222;

">

%s

</h2>

<p style="

font-size:16px;

line-height:1.8;

color:#666;

">

Dear <strong>%s</strong>,

</p>

<p style="

font-size:16px;

line-height:1.9;

color:#555;

">

%s

</p>

<div style="

margin:35px 0;

padding:25px;

background:#F8F5FF;

border-left:6px solid #4B2E83;

border-radius:16px;

">

<div style="

font-size:14px;

color:#777;

">

%s

</div>

<div style="

margin-top:10px;

font-size:30px;

font-weight:bold;

letter-spacing:2px;

color:#4B2E83;

">

%s

</div>

</div>

%s

<hr style="

border:none;

border-top:1px solid #ECECEC;

margin:40px 0;

">

<p style="

font-size:14px;

line-height:1.8;

color:#777;

">

Need assistance?

<br>

Email:
support@coastal.go.tz

<br>

Phone:
+255 777 61 77 86

</p>

<p style="

text-align:center;

margin-top:35px;

font-size:13px;

color:#999;

">

© 2026

Government of Zanzibar

<br>

ICT-Based Coastal Conservation & Revenue Monitoring System

</p>

</div>

</div>

</body>

</html>

""".formatted(

                    title,

                    fullName,

                    message,

                    highlightTitle,

                    highlightValue,

                    buttonLink == null || buttonLink.isBlank()

                            ?

                            ""

                            :

                            """

<div style="text-align:center;margin-top:35px;">

<a href="%s"

style="

display:inline-block;

padding:15px 35px;

background:#F28B65;

color:white;

text-decoration:none;

border-radius:12px;

font-weight:600;

">

%s

</a>

</div>

""".formatted(

                                    buttonLink,

                                    buttonText

                            )

            ), true);

            mailSender.send(mimeMessage);

        }

        catch (Exception ex){

            ex.printStackTrace();

        }

    }

}