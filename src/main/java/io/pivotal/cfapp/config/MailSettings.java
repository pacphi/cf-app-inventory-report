package io.pivotal.cfapp.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix="spring.mail")
public class MailSettings {

    private String username;
    private List<String> recipients;
    private String subject;
    
}
