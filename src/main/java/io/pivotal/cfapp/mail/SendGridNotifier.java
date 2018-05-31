package io.pivotal.cfapp.mail;

import java.io.IOException;
import java.util.Base64;

import javax.mail.MessagingException;

import org.springframework.http.HttpStatus;

import com.sendgrid.Attachments;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.config.MailSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendGridNotifier extends AppNotifier {
    
    private SendGrid sendGrid;
    
    public SendGridNotifier(
            AppSettings appSettings, MailSettings mailSettings, SendGrid sendGrid) {
        super(appSettings, mailSettings);
        this.sendGrid = sendGrid;
    }
    
    protected void sendMail(
            String recipient, String subject, String body, 
            String detailAttachment, String summaryAttachment) 
                    throws MessagingException, IOException {
        Email from = new Email(mailSettings.getFrom());
        Email to = new Email(recipient);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);
        Attachments detail = new Attachments();
        detail.setContent(new String(Base64.getEncoder().encode(detailAttachment.getBytes())));
        detail.setType("text/csv");
        detail.setFilename("app-inventory-detail.csv");
        detail.setDisposition("attachment");
        detail.setContentId("App Inventory (Detail)");
        Attachments summary = new Attachments();
        summary.setContent(new String(Base64.getEncoder().encode(summaryAttachment.getBytes())));
        summary.setType("text/csv");
        summary.setFilename("app-inventory-summary.csv");
        summary.setDisposition("attachment");
        summary.setContentId("App Inventory (Summary)");
        mail.addAttachments(detail);
        mail.addAttachments(summary);
        
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);
        log.info(HttpStatus.valueOf(response.getStatusCode()).getReasonPhrase());
    }
}