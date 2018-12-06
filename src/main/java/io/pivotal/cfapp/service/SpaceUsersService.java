package io.pivotal.cfapp.service;

import io.pivotal.cfapp.domain.SpaceUsers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SpaceUsersService {

	Mono<Void> deleteAll();

	Mono<SpaceUsers> save(SpaceUsers entity);

	Flux<SpaceUsers> findAll();

	Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space);

}