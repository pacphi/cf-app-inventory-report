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
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppMetrics;
import io.pivotal.cfapp.domain.BuildpackCount;

@Component
public class AppNotifier implements ApplicationListener<AppInfoRetrievedEvent> {

    private Logger log = LoggerFactory.getLogger(AppNotifier.class);
    
    private AppSettings appSettings;
    private MailSettings mailSettings;
    private JavaMailSender javaMailSender;
    
    @Autowired
    public AppNotifier(
            AppSettings appSettings, MailSettings mailSettings, 
            JavaMailSender javaMailSender) {
        this.appSettings = appSettings;
        this.mailSettings = mailSettings;
        this.javaMailSender = javaMailSender;
    }

    protected void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mailSettings.getUsername());
        helper.setSubject(subject);
        helper.setTo(to);
        helper.setText(body, true);
        DataSource detail = new ByteArrayDataSource(detailAttachment, "text/csv");
        DataSource summary = new ByteArrayDataSource(summaryAttachment, "text/csv");
        helper.addAttachment("app-inventory-detail.csv", detail);
        helper.addAttachment("app-inventory-summary.csv", summary);
        javaMailSender.send(message);
    }

    @Override
    public void onApplicationEvent(AppInfoRetrievedEvent event) {
        String body = applyBody();
        String detailAttachment = applyDetailAttachment(event);
        String summaryAttachment = applySummaryAttachment(event);
        log.info(detailAttachment);
        log.info(summaryAttachment);
        mailSettings.getRecipients().forEach(r -> {
            try {
                sendMail(r, mailSettings.getSubject(), body, detailAttachment, summaryAttachment);
            } catch (MessagingException | IOException e) {
                log.error("Could not send email!", e);
            }
        });
        
    }

    private String applyBody() {
        StringBuffer body = new StringBuffer();
        body.append("Please find attached application inventory detail and summary reports from ");
        body.append(appSettings.getApiHost());
        body.append(" generated ");
        body.append(LocalDateTime.now());
        body.append(".");
        return body.toString();
    }
    
    private String applyDetailAttachment(AppInfoRetrievedEvent event) {
        StringBuffer attachment = new StringBuffer();
        attachment.append("\n");
        attachment.append(AppDetail.headers());
        attachment.append("\n");
        event.getDetail()
                .forEach(a -> { 
                    attachment.append(a.toCsv());
                    attachment.append("\n");
                });
        return attachment.toString();
    }
    
    private String applySummaryAttachment(AppInfoRetrievedEvent event) {
        AppMetrics metrics = new AppMetrics(event.getDetail());
        StringBuffer attachment = new StringBuffer();
        attachment.append("\n");
        attachment.append(BuildpackCount.headers());
        attachment.append("\n");
        event.getBuildpackCounts().forEach(r -> {
            attachment.append(r.toCsv());
            attachment.append("\n");
        });
    
        attachment.append("\n");
        attachment.append(AppMetrics.pushHeaders() + "\n");
        attachment.append("<= 1 day," + metrics.pushedInLastDay() + "\n");
        attachment.append("> 1 day <= 1 week," + metrics.pushedInLastWeek() + "\n");
        attachment.append("> 1 week <= 1 month," + metrics.pushedInLastMonth() + "\n");
        attachment.append("> 1 month <= 3 months," + metrics.pushedInLastThreeMonths() + "\n");
        attachment.append("> 3 months <= 6 months," + metrics.pushedInLastSixMonths() + "\n");
        attachment.append("> 6 months <= 1 year," + metrics.pushedInLastYear() + "\n");
        attachment.append("> 1 year," + metrics.pushedBeyondOneYear() + "\n");
        
        attachment.append("\n");
        attachment.append(AppMetrics.instanceStateHeaders() + "\n");
        attachment.append("started," + metrics.totalStartedInstances()  + "\n");
        attachment.append("stopped," + metrics.totalStoppedInstances() + "\n");
        attachment.append("all," + metrics.totalApplicationInstances() + "\n");
        
        attachment.append("\n");
        attachment.append("Total applications: " + metrics.totalApplications());
        return attachment.toString();
    }
}