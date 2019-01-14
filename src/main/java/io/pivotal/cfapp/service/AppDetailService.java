package io.pivotal.cfapp.service;

import java.util.List;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AppDetailService {

	Mono<Void> deleteAll();

	Mono<AppDetail> save(AppDetail entity);

	Flux<AppDetail> findAll();

	List<BuildpackCount> countApplicationsByBuildpack();

	List<OrganizationCount> countApplicationsByOrganization();

	List<DockerImageCount> countApplicationsByDockerImage();

}