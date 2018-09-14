package io.pivotal.cfapp.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import io.pivotal.cfapp.domain.SpaceUsers;

@Profile("mongo")
public interface MongoSpaceUsersRepository extends ReactiveCrudRepository<SpaceUsers, String> {
}