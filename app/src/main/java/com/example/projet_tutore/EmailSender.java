package com.example.projet_tutore;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {

    public static void sendEmail(String toEmail, String subject, String messageBody) {

        new Thread(() -> { // ⚡ Important : thread séparé pour Android
            final String fromEmail = "welcometogreengo@gmail.com"; // ton email Gmail
            final String password = "fqpa nkoe vnbn hiyu";     // mot de passe d'application Gmail

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromEmail, password);
                        }
                    });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setText(messageBody);

                Transport.send(message);

                System.out.println("Email envoyé avec succès !");
            } catch (Exception e) {
                e.printStackTrace(); // ⚠️ Vérifie Logcat si erreur
            }
        }).start();
    }
}