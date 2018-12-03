package io.pivotal.cfapp.task;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.useradmin.ListSpaceUsersRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppRequest;
import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import reactor.core.publisher.Flux;

@Component
public class SpaceUsersTask implements ApplicationRunner {

    private DefaultCloudFoundryOperations opsClient;
    private SpaceUsersService service;

    @Autowired
    public SpaceUsersTask(
    		DefaultCloudFoundryOperations opsClient,
    		SpaceUsersService service) {
        this.opsClient = opsClient;
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        runTask();
    }

    @Scheduled(cron = "${cron}")
    protected void runTask() {
        service
            .deleteAll()
            .thenMany(getOrganizations())
            .flatMap(spaceRequest -> getSpaces(spaceRequest))
            .flatMap(spaceUsersRequest -> getSpaceUsers(spaceUsersRequest))
            .flatMap(service::save)
            .subscribe();
    }

    protected Flux<AppRequest> getOrganizations() {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .build()
                .organizations()
                    .list()
                    .map(os -> AppRequest.builder().organization(os.getName()).build());
    }

    protected Flux<AppRequest> getSpaces(AppRequest request) {
        return DefaultCloudFoundryOperations.builder()
            .from(opsClient)
            .organization(request.getOrganization())
            .build()
                .spaces()
                    .list()
                    .map(ss -> AppRequest.from(request).space(ss.getName()).build());
    }

    protected Flux<SpaceUsers> getSpaceUsers(AppRequest request) {
    	return DefaultCloudFoundryOperations.builder()
                .from(opsClient)
                .organization(request.getOrganization())
                .space(request.getSpace())
                .build()
                	.userAdmin()
                		.listSpaceUsers(
                				ListSpaceUsersRequest
                					.builder()
                						.organizationName(request.getOrganization())
                						.spaceName(request.getSpace()).build()
                		)
                		.flux()
                		.map(su -> SpaceUsers
                						.builder()
                							.organization(request.getOrganization())
                							.space(request.getSpace())
                							.auditors(su.getAuditors())
                							.managers(su.getManagers())
                							.developers(su.getDevelopers())
                							.build()
						);
    }

}
