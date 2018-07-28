package io.pivotal.cfapp.repository.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import io.pivotal.cfapp.domain.AppDetail;

@Profile("mongo")
public interface MongoAppInfoRepository extends ReactiveCrudRepository<AppDetail, String> {
}