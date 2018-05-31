package io.pivotal.cfapp.task;

import java.time.ZoneId;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.AppRequest;
import io.pivotal.cfapp.domain.Buildpack;
import io.pivotal.cfapp.repository.AppDetailAggregator;
import io.pivotal.cfapp.repository.ReactiveAppInfoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AppTask implements ApplicationRunner {
    
    private DefaultCloudFoundryOperations opsClient;
    private ApplicationEventPublisher applicationEventPublisher;
    private ReactiveAppInfoRepository reactiveAppInfoRepository;
    private AppDetailAggregator appDetailAggregator;
    
    @Autowired
    public AppTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher,
            ReactiveAppInfoRepository reactiveAppInfoRepository,
            AppDetailAggregator appDetailAggregator
            ) {
        this.opsClient = opsClient;
        this.applicationEventPublisher = applicationEventPublisher;
        this.reactiveAppInfoRepository = reactiveAppInfoRepository;
        this.appDetailAggregator = appDetailAggregator;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
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

    private Flux<AppRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> AppRequest.builder().organization(os.getName()).build())
                    .log();
    }
    
    private Flux<AppRequest> getSpaces(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(ss -> AppRequest.from(request).space(ss.getName()).build())
                    .log();
    }
    
    private Flux<AppRequest> getApplicationSummary(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .list()
                    .map(as -> AppRequest.from(request).appName(as.getName()).build())
                    .log();
    }
    
    // Added onErrorResume as per https://stackoverflow.com/questions/48243630/is-there-a-way-in-reactor-to-ignore-error-signals
    // to address org.cloudfoundry.client.v2.ClientV2Exception: CF-NoAppDetectedError(170003): An app was not successfully detected by any available buildpack
    // which results in some undesirable but tolerable data loss
    private Mono<AppDetail> getApplicationDetail(AppRequest request) {
         return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .get(GetApplicationRequest.builder().name(request.getAppName()).build())
                    .onErrorResume(e -> Mono.empty())
                    .map(a -> AppDetail
                                .builder()
                                    .organization(request.getOrganization())
                                    .space(request.getSpace())
                                    .appName(request.getAppName())
                                    .buildpack(Buildpack.is(a.getBuildpack()))
                                    .stack(a.getStack())
                                    .runningInstances(a.getRunningInstances())
                                    .totalInstances(a.getInstances())
                                    .urls(String.join(",", a.getUrls()))
                                    .lastPushed(a.getLastUploaded()
                                                .toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime())
                                    .requestedState(a.getRequestedState().toLowerCase())
                                    .build())
                    .log();
    }

    private Mono<AppDetail> enrichWithAppEvent(AppDetail detail) {
        return DefaultCloudFoundryOperations.builder()
           .from(opsClient)
           .organization(detail.getOrganization())
           .space(detail.getSpace())
           .build()
               .applications()
                   .getEvents(GetApplicationEventsRequest.builder().name(detail.getAppName()).build())
                       .map(e -> AppEvent
                                   .builder()
                                       .name(e.getEvent())
                                       .actor(e.getActor())
                                       .time(e.getTime())
                                       .build())
                       .next()
                       .map(e -> 
                               AppDetail.from(detail)
                                           .lastEvent(e.getName())
                                           .lastEventActor(e.getActor())
                                           .build()
                           )
                       .switchIfEmpty(Mono.just(detail))
                       .log();
    }
}
