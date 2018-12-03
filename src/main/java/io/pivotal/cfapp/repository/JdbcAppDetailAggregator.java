package io.pivotal.cfapp.repository;

import java.util.ArrayList;
import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;

@Profile("jdbc")
@Component
public class JdbcAppDetailAggregator implements AppDetailAggregator {

	private Database database;

	@Autowired
	public JdbcAppDetailAggregator(Database database) {
		this.database = database;
	}

	@Override
	public List<BuildpackCount> countApplicationsByBuildpack() {
		List<BuildpackCount> result = new ArrayList<>();
		Flowable<BuildpackCount> records = database
			.select("SELECT buildpack, COUNT(id) AS total FROM app_detail GROUP BY buildpack")
			.get(rs -> new BuildpackCount(rs.getString(1), rs.getInt(2)));
		Flux.from(records).subscribe(result::add);
		return result;
	}

	@Override
	public List<OrganizationCount> countApplicationsByOrganization() {
		List<OrganizationCount> result = new ArrayList<>();
		Flowable<OrganizationCount> records = database
				.select("SELECT organization, COUNT(id) AS total FROM app_detail WHERE organization IS NOT NULL GROUP BY organization")
				.get(rs -> new OrganizationCount(rs.getString(1), rs.getInt(2)));
		Flux.from(records).subscribe(result::add);
		return result;
	}

	@Override
	public List<DockerImageCount> countApplicationsByDockerImage() {
		List<DockerImageCount> result = new ArrayList<>();
		Flowable<DockerImageCount> records = database
				.select("SELECT image, COUNT(id) AS total FROM app_detail WHERE image IS NOT NULL GROUP BY image")
				.get(rs -> new DockerImageCount(rs.getString(1), rs.getInt(2)));
		Flux.from(records).subscribe(result::add);
		return result;
	}

}
