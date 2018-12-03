package io.pivotal.cfapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.repository.AppDetailAggregator;
import io.pivotal.cfapp.repository.JdbcAppInfoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcAppInfoService implements AppInfoService {

	private JdbcAppInfoRepository repo;
	private AppDetailAggregator aggregator;

	@Autowired
	public JdbcAppInfoService(JdbcAppInfoRepository repo, AppDetailAggregator aggregator) {
		this.repo = repo;
		this.aggregator = aggregator;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<AppDetail> save(AppDetail entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<AppDetail> findAll() {
		return repo.findAll();
	}

	@Override
	public List<BuildpackCount> countApplicationsByBuildpack() {
		return aggregator.countApplicationsByBuildpack();
	}

	@Override
	public List<OrganizationCount> countApplicationsByOrganization() {
		return aggregator.countApplicationsByOrganization();
	}

	@Override
	public List<DockerImageCount> countApplicationsByDockerImage() {
		return aggregator.countApplicationsByDockerImage();
	}
}
