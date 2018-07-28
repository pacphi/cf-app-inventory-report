package io.pivotal.cfapp.task.jdbc;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.repository.AppDetailAggregator;
import io.pivotal.cfapp.repository.jdbc.JdbcAppInfoRepository;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import io.pivotal.cfapp.task.AppTask;

@Profile("jdbc")
@Component
public class JdbcAppTask extends AppTask {
    
    private ApplicationEventPublisher applicationEventPublisher;
    private JdbcAppInfoRepository reactiveAppInfoRepository;
    private AppDetailAggregator appDetailAggregator;
    
    @Autowired
    public JdbcAppTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher,
            JdbcAppInfoRepository reactiveAppInfoRepository,
            AppDetailAggregator appDetailAggregator
            ) {
    	super(opsClient);
        this.applicationEventPublisher = applicationEventPublisher;
        this.reactiveAppInfoRepository = reactiveAppInfoRepository;
        this.appDetailAggregator = appDetailAggregator;
    }

    @Override
    public void runTask() {
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
