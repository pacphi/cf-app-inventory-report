package io.pivotal.cfapp.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import io.pivotal.cfapp.domain.AppDetail;
 
public interface ReactiveAppInfoRepository extends ReactiveCrudRepository<AppDetail, String> {
}