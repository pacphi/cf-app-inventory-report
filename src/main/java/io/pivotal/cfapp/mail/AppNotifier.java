package io.pivotal.cfapp.mail;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.mail.MessagingException;

import org.springframework.context.ApplicationListener;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.config.MailSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppMetrics;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AppNotifier implements ApplicationListener<AppInfoRetrievedEvent> {
    
    protected final AppSettings appSettings;
    protected final MailSettings mailSettings;
    
    public AppNotifier(AppSettings appSettings, MailSettings mailSettings) {
        this.appSettings = appSettings;
        this.mailSettings = mailSettings;
    }

    protected abstract void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException;

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
        attachment.append(OrganizationCount.headers());
        attachment.append("\n");
        event.getOrganizationCounts().forEach(r -> {
            attachment.append(r.toCsv());
            attachment.append("\n");
        });
        
        attachment.append("\n");
        attachment.append(BuildpackCount.headers());
        attachment.append("\n");
        event.getBuildpackCounts().forEach(r -> {
            attachment.append(r.toCsv());
            attachment.append("\n");
        });
        
        attachment.append("\n");
        attachment.append(DockerImageCount.headers());
        attachment.append("\n");
        event.getDockerImages().forEach(r -> {
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