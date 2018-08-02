package io.pivotal.cfapp.task;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationEventsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppEvent;
import io.pivotal.cfapp.domain.AppRequest;
import io.pivotal.cfapp.domain.Buildpack;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AppTask implements ApplicationRunner {
    
    private DefaultCloudFoundryOperations opsClient;
    
    @Autowired
    public AppTask(DefaultCloudFoundryOperations opsClient) {
        this.opsClient = opsClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        runTask();
    }

    protected abstract void runTask();
    
    protected Flux<AppRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> AppRequest.builder().organization(os.getName()).build())
                    .log();
    }
    
    protected Flux<AppRequest> getSpaces(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(ss -> AppRequest.from(request).space(ss.getName()).build())
                    .log();
    }
    
    protected Flux<AppRequest> getApplicationSummary(AppRequest request) {
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
    protected Mono<AppDetail> getApplicationDetail(AppRequest request) {
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
                                    .lastPushed(a.getLastUploaded() != null ? a.getLastUploaded()
                                                .toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime(): LocalDateTime.of(1400, 1,1,12,0,0,0))
                                    .requestedState(a.getRequestedState().toLowerCase())
                                    .build())
                    .log();
    }

    protected Mono<AppDetail> enrichWithAppEvent(AppDetail detail) {
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
