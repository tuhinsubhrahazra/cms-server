/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author Tuhin
 */

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {

    public static void main(String[] args) {
        String smtpHost = "smtp-relay.brevo.com";
        String smtpPort = "587";
        String smtpUsername = "8bb02f001@smtp-brevo.com";
        String smtpPassword = "Lc82h6dkEKqtwCrZ";

        String recipient = "tuhinhazra094@gmail.com";
        String subject = "Test Email (Old-School JavaMail)";
        String body = "<h1>Hello!</h1><p>This is an HTML email sent via classic JavaMail API.</p>";

        sendEmail(smtpHost, smtpPort, smtpUsername, smtpPassword, recipient, subject, body);
    }

    public static void sendEmail(String host, String port, String username, String password,
                                 String to, String subject, String htmlBody) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlBody, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Email sent successfully (old-school JavaMail)");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
