package io.pivotal.cfapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.store.IArtifactStore;
import io.pivotal.cfapp.config.AdditionalEmbeddedMongoProperties;
 
 
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@EnableReactiveMongoRepositories
@AutoConfigureAfter(EmbeddedMongoAutoConfiguration.class)
public class MongoConfig extends AbstractReactiveMongoConfiguration {
    private final Environment environment;
 
    public MongoConfig(Environment environment) {
        this.environment = environment;
    }
 
    @Override
    @Bean
    @DependsOn("embeddedMongoServer")
    public MongoClient reactiveMongoClient() {
        int port = environment.getProperty("local.mongo.port", Integer.class);
        return MongoClients.create(String.format("mongodb://localhost:%d", port));
    }
    
    @Bean
    public SimpleReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(
            ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory,
            MongoConverter converter) {
        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory, converter);
    }
 
    @Override
    protected String getDatabaseName() {
        return "cf-application-inventory";
    }

    @ConditionalOnProperty(prefix="spring.mongodb.embedded.download", name = "alternate", havingValue = "true")
    @Bean
    IRuntimeConfig embeddedMongoRuntimeConfig(AdditionalEmbeddedMongoProperties settings) {
    	Command command = Command.MongoD;
    	Logger logger = LoggerFactory
				.getLogger(getClass().getPackage().getName() + ".EmbeddedMongo");
		ProcessOutput processOutput = new ProcessOutput(
				Processors.logTo(logger, Slf4jLevel.INFO),
				Processors.logTo(logger, Slf4jLevel.ERROR), Processors.named(
						"[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)));
    	IDownloadConfig downloadConfig = 
    			new DownloadConfigBuilder()
    					.defaultsForCommand(command)
    					.downloadPath(settings.getPath())
    					.progressListener(new Slf4jProgressListener(logger))
						.build();
    	IArtifactStore artifactStore = 
    			new ExtractedArtifactStoreBuilder()
    					.defaults(command)
    					.download(downloadConfig)
    					.build();
    	return 
    			new RuntimeConfigBuilder()
    					.defaultsWithLogger(command, logger)
    					.processOutput(processOutput)
    					.artifactStore(artifactStore)
    					.build();
    }
}
