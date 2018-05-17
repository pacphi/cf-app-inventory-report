package io.pivotal.cfapp.task;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.Buildpack;
import io.pivotal.cfapp.domain.BuildpackCount;
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
        List<AppDetail> detail = new ArrayList<>();
        List<BuildpackCount> buildpackCounts = new ArrayList<>();
        getOrganizations().toStream()
            .forEach(o -> getSpaces(o).toStream()
                    .forEach(s -> getApplications(o, s).toStream()
                            .forEach(a -> getApplicationDetail(o, s, a)
                                            .subscribe(detail::add))));
        reactiveAppInfoRepository.saveAll(detail).subscribe();
        appDetailAggregator.countApplicationsByBuildpack().forEach(bc -> buildpackCounts.add(bc));
        AppInfoRetrievedEvent event = new AppInfoRetrievedEvent(this, detail, buildpackCounts);
        applicationEventPublisher.publishEvent(event);
    }

    private Flux<String> getOrganizations() {
        return opsClient.organizations()
                .list()
                .map(OrganizationSummary::getName);
    }
    
    private Flux<String> getSpaces(String organization) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(organization)
                .build()
                    .spaces()
                        .list()
                        .map(SpaceSummary::getName);
    }
    
    private Flux<String> getApplications(String organization, String space) {
        return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(organization)
                .space(space)
                .build()
                    .applications()
                        .list()
                        .map(ApplicationSummary::getName);
    }
    
    private Mono<AppDetail> getApplicationDetail(String organization, String space, String appName) {
             return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(organization)
                .space(space)
                .build()
                    .applications()
                        .get(GetApplicationRequest.builder().name(appName).build())
                        .map(a -> AppDetail
                                    .builder()
                                        .organization(organization)
                                        .space(space)
                                        .appName(appName)
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
                                        .build());
    }

}
