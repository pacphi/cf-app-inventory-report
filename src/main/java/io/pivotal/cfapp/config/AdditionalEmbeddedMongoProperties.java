package io.pivotal.cfapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.Data;

@Profile("mongo")
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.mongodb.embedded.download")
public class AdditionalEmbeddedMongoProperties {

	private String path;
	
}
