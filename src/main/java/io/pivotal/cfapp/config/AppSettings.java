package io.pivotal.cfapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cf")
public class AppSettings {

    private String apiHost;
    private boolean sslValidationSkipped;
    private String username;
    private String password;
    // this is the value of RefreshToken within ~/.cf/config.json after one authenticates w/ cf login -a {api_endpoint} -sso
    private String refreshToken;

}
