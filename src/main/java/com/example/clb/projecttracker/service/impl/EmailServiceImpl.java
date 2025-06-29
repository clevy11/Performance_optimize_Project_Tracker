package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.subjectPrefix}")
    private String subjectPrefix;

    @Override
    @Async
    public void sendSimpleMessage(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subjectPrefix + " " + subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent simple email to {} with subject: {}", to, subject);
        } catch (MailException e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlMessage(String to, String subject, String htmlBody) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subjectPrefix + " " + subject);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            log.info("Sent HTML email to {} with subject: {}", to, subject);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }
}