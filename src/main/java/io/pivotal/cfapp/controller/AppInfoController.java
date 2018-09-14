package io.pivotal.cfapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.report.CsvReport;
import io.pivotal.cfapp.service.AppInfoService;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import reactor.core.publisher.Mono;

@RestController
public class AppInfoController {

	private AppInfoService service;
	private CsvReport report;
	
	@Autowired
	public AppInfoController(
			AppSettings appSettings,
			AppInfoService service) {
		this.report = new CsvReport(appSettings);
		this.service = service;
	}
	
	@GetMapping(value = { "/report" }, produces = MediaType.TEXT_PLAIN_VALUE )
	public Mono<String> generateReport() {
		return service
				.findAll()
				.collectList()
		        .map(r ->
	                new AppInfoRetrievedEvent(
                        this, 
                        r, 
                        service.countApplicationsByBuildpack(),
                        service.countApplicationsByOrganization(),
                        service.countApplicationsByDockerImage()
	                )
		        )
		        .map(event -> 
		        	String.join(
		        			"\n\n", 
		        			report.generatePreamble(), 
		        			report.generateDetail(event), 
		        			report.generateSummary(event)));
	}

}
