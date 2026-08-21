package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Component
public class EmailNotificationSender implements NotificationSender {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String fromAddress;
    private final String fromName;
    private final boolean startTls;

    public EmailNotificationSender(
            @Value("${notification.email.smtp.host:}") String host,
            @Value("${notification.email.smtp.port:587}") int port,
            @Value("${notification.email.smtp.username:}") String username,
            @Value("${notification.email.smtp.password:}") String password,
            @Value("${notification.email.smtp.from-address:}") String fromAddress,
            @Value("${notification.email.smtp.from-name:Cyan Coder}") String fromName,
            @Value("${notification.email.smtp.starttls:true}") boolean startTls
    ) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.startTls = startTls;
    }

    @Override
    public boolean supports(String channel, String provider) {
        return "EMAIL".equalsIgnoreCase(channel) && (provider == null || provider.isBlank() || "default".equalsIgnoreCase(provider) || "smtp".equalsIgnoreCase(provider));
    }

    @Override
    public NotificationSendResult send(NotificationDispatchRequest request, String subject, String body) {
        if (host == null || host.isBlank() || fromAddress == null || fromAddress.isBlank()) {
            return new NotificationSendResult(false, "smtp", "", "NOT_CONFIGURED", "SMTP provider is not configured");
        }
        if (request.recipient() == null || request.recipient().isBlank()) {
            return new NotificationSendResult(false, "smtp", "", "FAILED", "Recipient email address is required");
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        boolean authenticated = username != null && !username.isBlank();
        if (authenticated) {
            mailSender.setUsername(username);
            mailSender.setPassword(password);
        }
        Properties mailProperties = mailSender.getJavaMailProperties();
        mailProperties.put("mail.transport.protocol", "smtp");
        mailProperties.put("mail.smtp.auth", authenticated);
        mailProperties.put("mail.smtp.starttls.enable", startTls);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(request.recipient());
            helper.setSubject(subject == null ? "" : subject);
            helper.setText(body == null ? "" : body, false);
            mailSender.send(message);
            return new NotificationSendResult(true, "smtp", "", "SENT", "");
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            return new NotificationSendResult(false, "smtp", "", "FAILED", "SMTP delivery failed");
        }
    }
}
