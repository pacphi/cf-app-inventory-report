package io.pivotal.cfapp.notifier;

import java.io.IOException;

import javax.mail.MessagingException;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.config.MailSettings;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EmailNotifier extends AppNotifier {
    
    protected final MailSettings mailSettings;
    
    public EmailNotifier(AppSettings appSettings, MailSettings mailSettings) {
        super(appSettings);
        this.mailSettings = mailSettings;
    }

    protected abstract void sendMail(String to, String subject, String body, String detailAttachment, String summaryAttachment) throws MessagingException, IOException;

    @Override
    public void onApplicationEvent(AppInfoRetrievedEvent event) {
        String body = applyBody();
        String detailAttachment = applyDetailAttachment(event);
        String summaryAttachment = applySummaryAttachment(event);
        mailSettings.getRecipients().forEach(r -> {
            try {
                sendMail(r, mailSettings.getSubject(), body, detailAttachment, summaryAttachment);
            } catch (MessagingException | IOException e) {
                log.error("Could not send email!", e);
            }
        });
        
    }

}