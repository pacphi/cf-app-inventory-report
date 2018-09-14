package io.pivotal.cfapp.report;

import java.time.LocalDateTime;

import io.pivotal.cfapp.config.AppSettings;
import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.AppMetrics;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.task.AppInfoRetrievedEvent;

public class CsvReport  {
    
	private AppSettings appSettings;
	
	public CsvReport(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

    public String generatePreamble() {
        StringBuffer preamble = new StringBuffer();
        preamble.append("Please find attached application inventory detail and summary reports from ");
        preamble.append(appSettings.getApiHost());
        preamble.append(" generated ");
        preamble.append(LocalDateTime.now());
        preamble.append(".");
        return preamble.toString();
    }
    
    public String generateDetail(AppInfoRetrievedEvent event) {
        StringBuffer details = new StringBuffer();
        details.append("\n");
        details.append(AppDetail.headers());
        details.append("\n");
        event.getDetail()
                .forEach(a -> { 
                    details.append(a.toCsv());
                    details.append("\n");
                });
        return details.toString();
    }
    
    public String generateSummary(AppInfoRetrievedEvent event) {
        AppMetrics metrics = new AppMetrics(event.getDetail());
        StringBuffer summary = new StringBuffer();
        
        summary.append("\n");
        summary.append(OrganizationCount.headers());
        summary.append("\n");
        event.getOrganizationCounts().forEach(r -> {
            summary.append(r.toCsv());
            summary.append("\n");
        });
        
        summary.append("\n");
        summary.append(BuildpackCount.headers());
        summary.append("\n");
        event.getBuildpackCounts().forEach(r -> {
            summary.append(r.toCsv());
            summary.append("\n");
        });
        
        summary.append("\n");
        summary.append(DockerImageCount.headers());
        summary.append("\n");
        event.getDockerImages().forEach(r -> {
        	summary.append(r.toCsv());
        	summary.append("\n");
        });
    
        summary.append("\n");
        summary.append(AppMetrics.pushHeaders() + "\n");
        summary.append("<= 1 day," + metrics.pushedInLastDay() + "\n");
        summary.append("> 1 day <= 1 week," + metrics.pushedInLastWeek() + "\n");
        summary.append("> 1 week <= 1 month," + metrics.pushedInLastMonth() + "\n");
        summary.append("> 1 month <= 3 months," + metrics.pushedInLastThreeMonths() + "\n");
        summary.append("> 3 months <= 6 months," + metrics.pushedInLastSixMonths() + "\n");
        summary.append("> 6 months <= 1 year," + metrics.pushedInLastYear() + "\n");
        summary.append("> 1 year," + metrics.pushedBeyondOneYear() + "\n");
        
        summary.append("\n");
        summary.append(AppMetrics.instanceStateHeaders() + "\n");
        summary.append("started," + metrics.totalStartedInstances()  + "\n");
        summary.append("stopped," + metrics.totalStoppedInstances() + "\n");
        summary.append("all," + metrics.totalApplicationInstances() + "\n");
        
        summary.append("\n");
        summary.append("Total applications: " + metrics.totalApplications());
        return summary.toString();
    }
}