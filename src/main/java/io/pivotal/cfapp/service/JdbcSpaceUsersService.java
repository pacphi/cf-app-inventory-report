package io.pivotal.cfapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.repository.JdbcSpaceUsersRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("jdbc")
@Service
public class JdbcSpaceUsersService implements SpaceUsersService {

	private JdbcSpaceUsersRepository repo;

	@Autowired
	public JdbcSpaceUsersService(JdbcSpaceUsersRepository repo) {
		this.repo = repo;
	}

	@Override
	public Mono<Void> deleteAll() {
		return repo.deleteAll();
	}

	@Override
	public Mono<SpaceUsers> save(SpaceUsers entity) {
		return repo.save(entity);
	}

	@Override
	public Flux<SpaceUsers> findAll() {
		return repo.findAll();
	}

}
