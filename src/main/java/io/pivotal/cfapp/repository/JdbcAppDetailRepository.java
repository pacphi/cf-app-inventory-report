package io.pivotal.cfapp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.pivotal.cfapp.domain.AppDetail;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcAppDetailRepository {

	private Database database;

	@Autowired
	public JdbcAppDetailRepository(Database database) {
		this.database = database;
	}

	public Mono<AppDetail> save(AppDetail entity) {
		String createOne = "insert into app_detail (organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Flowable<Integer> insert = database
			.update(createOne)
			.parameters(
				entity.getOrganization(),
				entity.getSpace(),
				entity.getAppId(),
				entity.getAppName(),
				entity.getBuildpack(),
				entity.getImage(),
				entity.getStack(),
				entity.getRunningInstances(),
				entity.getTotalInstances(),
				entity.getUrls(),
				entity.getLastPushed() != null ? Timestamp.valueOf(entity.getLastPushed()): null,
				entity.getLastEvent(),
				entity.getLastEventActor(),
				entity.getLastEventTime() != null ? Timestamp.valueOf(entity.getLastEventTime()): null,
				entity.getRequestedState()
			)
			.returnGeneratedKeys()
			.getAs(Integer.class);

		String selectOne = "select id, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from app_detail where id = ?";
		Flowable<AppDetail> result = database
			.select(selectOne)
			.parameterStream(insert)
			.get(rs -> fromResultSet(rs));
		return Mono.from(result);
	}

	public Flux<AppDetail> findAll() {
		String selectAll = "select id, organization, space, app_id, app_name, buildpack, image, stack, running_instances, total_instances, urls, last_pushed, last_event, last_event_actor, last_event_time, requested_state from app_detail order by organization, space, app_name";
		Flowable<AppDetail> result = database
			.select(selectAll)
			.get(rs -> fromResultSet(rs));
		return Flux.from(result);
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from app_detail";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}
	private AppDetail fromResultSet(ResultSet rs) throws SQLException {
		return AppDetail
				.builder()
					.id(String.valueOf(rs.getInt(1)))
					.organization(rs.getString(2))
					.space(rs.getString(3))
					.appId(rs.getString(4))
					.appName(rs.getString(5))
					.buildpack(rs.getString(6))
					.image(rs.getString(7))
					.stack(rs.getString(8))
					.runningInstances(rs.getInt(9))
					.totalInstances(rs.getInt(10))
					.urls(rs.getString(11))
					.lastPushed(rs.getTimestamp(12) != null ? rs.getTimestamp(12).toLocalDateTime(): null)
					.lastEvent(rs.getString(13))
					.lastEventActor(rs.getString(14))
					.lastEventTime(rs.getTimestamp(15) != null ? rs.getTimestamp(15).toLocalDateTime(): null)
					.requestedState(rs.getString(16))
					.build();
	}
}
