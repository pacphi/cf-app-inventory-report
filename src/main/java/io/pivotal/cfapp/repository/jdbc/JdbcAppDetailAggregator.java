package io.pivotal.cfapp.repository.jdbc;

import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.repository.AppDetailAggregator;

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
		return database
			.select("SELECT buildpack, COUNT(id) AS total FROM app_detail GROUP BY buildpack")
			.get(rs -> new BuildpackCount(rs.getString(1), rs.getInt(2)))
			.toList()
			.blockingGet();
	}

	@Override
	public List<OrganizationCount> countApplicationsByOrganization() {
		return database
				.select("SELECT organization, COUNT(id) AS total FROM app_detail GROUP BY organization")
				.get(rs -> new OrganizationCount(rs.getString(1), rs.getInt(2)))
				.toList()
				.blockingGet();
	}

}
