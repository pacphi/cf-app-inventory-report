package io.pivotal.cfapp.task.mongo;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.repository.AppDetailAggregator;
import io.pivotal.cfapp.repository.mongo.MongoAppInfoRepository;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import io.pivotal.cfapp.task.AppTask;

@Profile("mongo")
@Component
public class MongoAppTask extends AppTask {
    
    private ApplicationEventPublisher applicationEventPublisher;
    private MongoAppInfoRepository reactiveAppInfoRepository;
    private AppDetailAggregator appDetailAggregator;
    
    @Autowired
    public MongoAppTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher,
            MongoAppInfoRepository reactiveAppInfoRepository,
            AppDetailAggregator appDetailAggregator
            ) {
        super(opsClient);
        this.applicationEventPublisher = applicationEventPublisher;
        this.reactiveAppInfoRepository = reactiveAppInfoRepository;
        this.appDetailAggregator = appDetailAggregator;
    }

    @Override
    protected void runTask() {
        reactiveAppInfoRepository
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(appSummaryRequest -> getApplicationSummary(appSummaryRequest))
            .flatMap(appDetailRequest -> getApplicationDetail(appDetailRequest))
            .flatMap(withLastEventRequest -> enrichWithAppEvent(withLastEventRequest))
            .flatMap(reactiveAppInfoRepository::save)
            .thenMany(reactiveAppInfoRepository.findAll())
            .collectList()
            .subscribe(r -> 
                applicationEventPublisher.publishEvent(
                    new AppInfoRetrievedEvent(
                            this, 
                            r, 
                            appDetailAggregator.countApplicationsByBuildpack(),
                            appDetailAggregator.countApplicationsByOrganization()
                    )
                )
            );
    }
}
