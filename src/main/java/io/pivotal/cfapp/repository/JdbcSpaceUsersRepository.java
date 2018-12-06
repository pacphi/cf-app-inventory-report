package io.pivotal.cfapp.repository;

import java.io.IOException;
import java.util.List;

import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Repository
public class JdbcSpaceUsersRepository {

	private Database database;
	private ObjectMapper mapper;

	@Autowired
	public JdbcSpaceUsersRepository(
			Database database,
			ObjectMapper mapper) {
		this.database = database;
		this.mapper = mapper;
	}

	public Mono<Void> deleteAll() {
		String deleteAll = "delete from space_users";
		Flowable<Integer> result = database
			.update(deleteAll)
			.counts();
		return Flux.from(result).then();
	}

	public Mono<SpaceUsers> save(SpaceUsers entity) {
		String createOne = "insert into space_users (organization, space, auditors, managers, developers) values (?, ?, ?, ?, ?)";
		Flowable<Integer> insert = database
			.update(createOne)
			.parameters(
				entity.getOrganization(),
				entity.getSpace(),
				toJson(entity.getManagers()),
				toJson(entity.getAuditors()),
				toJson(entity.getDevelopers())
			)
			.returnGeneratedKeys()
			.getAs(Integer.class);

		String selectOne = "select id, organization, space, auditors, managers, developers from space_users where id = ?";
		Flowable<SpaceUsers> result = database
			.select(selectOne)
			.parameterStream(insert)
			.get(rs -> SpaceUsers
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
						.auditors(toList(rs.getString(4)))
						.managers(toList(rs.getString(5)))
						.developers(toList(rs.getString(6)))
						.build());
		return Mono.from(result);
	}

	public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
		String selectOne = "select id, organization, space, auditors, managers, developers from space_users where organization = ? and space = ?";
		Flowable<SpaceUsers> result = database
			.select(selectOne)
			.parameters(organization, space)
			.get(rs -> SpaceUsers
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
						.auditors(toList(rs.getString(4)))
						.managers(toList(rs.getString(5)))
						.developers(toList(rs.getString(6)))
						.build());
		return Mono.from(result);
	}

	public Flux<SpaceUsers> findAll() {
		String selectAll = "select id, organization, space, auditors, managers, developers from space_users order by organization, space";
		Flowable<SpaceUsers> result = database
			.select(selectAll)
			.get(rs -> SpaceUsers
						.builder()
						.id(String.valueOf(rs.getInt(1)))
						.organization(rs.getString(2))
						.space(rs.getString(3))
						.auditors(toList(rs.getString(4)))
						.managers(toList(rs.getString(5)))
						.developers(toList(rs.getString(6)))
						.build());
		return Flux.from(result);
	}

	private String toJson(List<String> list) {
		try {
			return mapper.writeValueAsString(list);
		} catch (JsonProcessingException jpe) {
			throw new RuntimeException(jpe);
		}
	}

	private List<String> toList(String json) {
		try {
			return mapper.readValue(
					json, new TypeReference<List<String>>() {});
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
