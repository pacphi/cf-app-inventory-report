package io.pivotal.cfapp.notifier;

import java.io.IOException;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.config.MailSettings;

public class JavaMailNotifier extends EmailNotifier {

    private final JavaMailSender javaMailSender;
    
    public JavaMailNotifier(
            AppSettings appSettings, MailSettings mailSettings, 
            JavaMailSender javaMailSender) {
        super(appSettings, mailSettings);
        this.javaMailSender = javaMailSender;
    }

    protected void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailSettings.getFrom());
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setText(body, true);
        DataSource detail = new ByteArrayDataSource(detailAttachment, "text/csv");
        DataSource summary = new ByteArrayDataSource(summaryAttachment, "text/csv");
        helper.addAttachment("app-inventory-detail.csv", detail);
        helper.addAttachment("app-inventory-summary.csv", summary);
        javaMailSender.send(message);
    }
}