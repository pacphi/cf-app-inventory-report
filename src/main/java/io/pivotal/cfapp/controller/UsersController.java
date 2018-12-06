package io.pivotal.cfapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UsersController {

	private final SpaceUsersService service;

	@Autowired
	public UsersController(
		SpaceUsersService service) {
		this.service = service;
	}

	@GetMapping("/users")
	public Mono<ResponseEntity<List<SpaceUsers>>> getAllUsers() {
		return service
					.findAll()
						.collectList()
							.map(users -> ResponseEntity.ok(users))
							.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/users/{organization}/{space}")
	public Mono<ResponseEntity<SpaceUsers>> getUsersInOrganizationAndSpace(
		@PathVariable("organization") String organization,
		@PathVariable("space") String space) {
		return service
					.findByOrganizationAndSpace(organization, space)
					.map(users -> ResponseEntity.ok(users))
					.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
