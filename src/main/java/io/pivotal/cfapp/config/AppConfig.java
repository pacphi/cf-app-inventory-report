package io.pivotal.cfapp.config;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.sendgrid.SendGrid;

import io.pivotal.cfapp.notifier.JavaMailNotifier;
import io.pivotal.cfapp.notifier.SendGridNotifier;

@EnableScheduling
@Configuration
public class AppConfig {

    @Bean
    DefaultConnectionContext connectionContext(AppSettings settings) {
        return DefaultConnectionContext.builder()
            .apiHost(settings.getApiHost())
            .build();
    }

    @Bean
    PasswordGrantTokenProvider tokenProvider(AppSettings settings) {
        return PasswordGrantTokenProvider.builder()
            .username(settings.getUsername())
            .password(settings.getPassword())
            .build();
    }

    @Bean
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
    return ReactorCloudFoundryClient.builder()
        .connectionContext(connectionContext)
        .tokenProvider(tokenProvider)
        .build();
}

    @Bean
    ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorUaaClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    DefaultCloudFoundryOperations opsClient(ReactorCloudFoundryClient cloudFoundryClient, 
            ReactorDopplerClient dopplerClient, ReactorUaaClient uaaClient) {
        return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .dopplerClient(dopplerClient)
                .uaaClient(uaaClient)
                .build();
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster 
          = new SimpleApplicationEventMulticaster();

        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }

    @Bean
    @ConditionalOnProperty(prefix="notification", name="engine", havingValue="java-mail")
    public JavaMailNotifier javaMailNotifier(
            AppSettings appSettings, MailSettings mailSettings, JavaMailSender javaMailSender) {
        return new JavaMailNotifier(appSettings, mailSettings, javaMailSender);
    }

    @Bean
    @ConditionalOnProperty(prefix="notification", name="engine", havingValue="sendgrid")
    public SendGridNotifier sendGridNotifier(
            AppSettings appSettings, MailSettings mailSettings, SendGrid sendGrid) {
        return new SendGridNotifier(appSettings, mailSettings, sendGrid);
    }
}
