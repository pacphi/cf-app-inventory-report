package io.pivotal.cfapp.task;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.repository.AppDetailAggregator;
import io.pivotal.cfapp.repository.ReactiveAppInfoRepository;
import reactor.core.publisher.Flux;

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
        List<AppRequest> requests = new ArrayList<>();
        List<AppDetail> detail = new ArrayList<>();
        List<AppDetail> enriched = new ArrayList<>();
        List<BuildpackCount> buildpackCounts = new ArrayList<>();
        getOrganizations()
            .flatMap(spacesRequest -> getSpaces(spacesRequest))
            .flatMap(appSummaryRequest -> getApplicationSummary(appSummaryRequest))
            .subscribe(requests::add);
        requests.forEach(appDetailRequest -> getApplicationDetail(appDetailRequest)
                                .subscribe(detail::add)
        );
        reactiveAppInfoRepository.saveAll(detail)
            .toStream()
                .forEach(ad -> { 
                    AppEvent event = getLastAppEvent(ad);
                    if (event != null) {
                        ad.setLastEvent(event.getName());
                        ad.setLastEventActor(event.getActor());
                    }
                    enriched.add(ad);
                });
        appDetailAggregator.countApplicationsByBuildpack().forEach(bc -> buildpackCounts.add(bc));
        AppInfoRetrievedEvent event = new AppInfoRetrievedEvent(this, enriched, buildpackCounts);
        applicationEventPublisher.publishEvent(event);
    }

    private Flux<AppRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> AppRequest.builder().organization(os.getName()).build());
    }
    
    private Flux<AppRequest> getSpaces(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(ss -> AppRequest.from(request).space(ss.getName()).build());
    }
    
    private Flux<AppRequest> getApplicationSummary(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .list()
                    .map(as -> AppRequest.from(request).appName(as.getName()).build());
    }
    
    private Flux<AppDetail> getApplicationDetail(AppRequest request) {
         return Flux.from(DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .space(request.getSpace())
            .build()
                .applications()
                    .get(GetApplicationRequest.builder().name(request.getAppName()).build())
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
                                    .build()));
    }

    private AppEvent getLastAppEvent(AppDetail detail) {
        Flux<AppEvent> events = DefaultCloudFoundryOperations.builder()
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
                                       .build());
        return events
                    .toStream()
                          .max(Comparator.comparing(AppEvent::getTime))
                              .orElse(null);
    }
}
