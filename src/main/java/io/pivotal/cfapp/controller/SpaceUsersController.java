package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.service.SpaceUsersService;
import reactor.core.publisher.Flux;

@RestController
public class SpaceUsersController {

	private SpaceUsersService service;

	@Autowired
	public SpaceUsersController(SpaceUsersService service) {
		this.service = service;
	}

	@GetMapping("/users")
	public Flux<SpaceUsers> getUsers() {
		return service.findAll();
	}
}
