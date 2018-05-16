package io.pivotal.cfapp.task;

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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AppTask implements ApplicationRunner {
    
    private DefaultCloudFoundryOperations opsClient;
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Autowired
    public AppTask(
            DefaultCloudFoundryOperations opsClient,
            ApplicationEventPublisher applicationEventPublisher
            ) {
        this.opsClient = opsClient;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<AppInfo> result = new ArrayList<>();
        getOrganizations().toStream()
            .forEach(o -> getSpaces(o).toStream()
                    .forEach(s -> getApplications(o, s).toStream()
                            .forEach(a -> getApplicationDetail(o, s, a)
                                            .subscribe(result::add))));
        AppInfoRetrievedEvent event = new AppInfoRetrievedEvent(this, result);
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
    
    private Mono<AppInfo> getApplicationDetail(String organization, String space, String appName) {
             return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(organization)
                .space(space)
                .build()
                    .applications()
                        .get(GetApplicationRequest.builder().name(appName).build())
                        .map(a -> new AppInfo(organization, space, appName, a.getBuildpack(), String.join("/", String.valueOf(a.getRunningInstances()), String.valueOf(a.getInstances())), String.join(",", a.getUrls())));
    }

}
