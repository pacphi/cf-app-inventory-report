package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.report.CsvReport;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsoleNotifier implements ApplicationListener<AppInfoRetrievedEvent> {

	private final CsvReport report;
	
    @Autowired
    public ConsoleNotifier(AppSettings appSettings) {
        this.report = new CsvReport(appSettings);
    }

	@Override
	public void onApplicationEvent(AppInfoRetrievedEvent event) {
		log.info(String.join("\n\n", report.generatePreamble(), report.generateDetail(event), report.generateSummary(event)));
	}

    
}