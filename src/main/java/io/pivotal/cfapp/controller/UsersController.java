package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
	public Flux<SpaceUsers> getAllUsers() {
		return service.findAll();
	}

	@GetMapping("/users/{organization}/{space}")
	public Mono<SpaceUsers> getUsersInOrganizationAndSpace(
		@PathVariable("organization") String organization,
		@PathVariable("space") String space) {
		return service.findByOrganizationAndSpace(organization, space);
	}
}
