package io.pivotal.cfapp.notifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConsoleNotifier extends AppNotifier {

    @Autowired
    public ConsoleNotifier(AppSettings appSettings) {
        super(appSettings);
    }

	@Override
	public void onApplicationEvent(AppInfoRetrievedEvent event) {
		log.info(String.join("\n\n", applyBody(), applyDetailAttachment(event), applySummaryAttachment(event)));
	}

    
}