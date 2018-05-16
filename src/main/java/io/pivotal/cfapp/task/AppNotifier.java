package io.pivotal.cfapp.task;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.config.MailSettings;

@Component
public class AppNotifier implements ApplicationListener<AppInfoRetrievedEvent> {

    private Logger log = LoggerFactory.getLogger(AppNotifier.class);
    
    private AppSettings appSettings;
    private MailSettings mailSettings;
    private JavaMailSender javaMailSender;
    
    @Autowired
    public AppNotifier(AppSettings appSettings, MailSettings mailSettings, JavaMailSender javaMailSender) {
        this.appSettings = appSettings;
        this.mailSettings = mailSettings;
        this.javaMailSender = javaMailSender;
    }

    protected void sendMail(String to, String subject, String body, String attachment) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailSettings.getUsername());
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setText(body, true);
        DataSource dataSource = new ByteArrayDataSource(attachment, "text/csv");
        helper.addAttachment("app-inventory.csv", dataSource);
        javaMailSender.send(message);
    }

    @Override
    public void onApplicationEvent(AppInfoRetrievedEvent event) {
        String body = applyBody();
        String attachment = applyAttachment(event);
        mailSettings.getRecipients().forEach(r -> {
            try {
                sendMail(r, mailSettings.getSubject(), body, attachment);
                log.info(attachment.toString());
            } catch (MessagingException | IOException e) {
                log.error("Could not send email!", e);
            }
        });
        
    }

    private String applyBody() {
        StringBuffer body = new StringBuffer();
        body.append("Please find attached an application inventory report from ");
        body.append(appSettings.getApiHost());
        body.append(" generated ");
        body.append(LocalDateTime.now());
        body.append(".");
        return body.toString();
    }
    
    private String applyAttachment(AppInfoRetrievedEvent event) {
        StringBuffer attachment = new StringBuffer();
        attachment.append(AppInfo.headers());
        attachment.append("\n");
        event.getInfo()
                .forEach(a -> { 
                    attachment.append(a.toString());
                    attachment.append("\n");
                });
        return attachment.toString();
    }
}