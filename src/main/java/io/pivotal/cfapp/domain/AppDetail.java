package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document
@Data
@Builder
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@NoArgsConstructor(access=AccessLevel.PACKAGE)
@ToString
public class AppDetail {
    
    @Id
    private String id;
    private String organization;
    private String space;
    private String appName;
    private String buildpack;
    private String stack;
    private Integer runningInstances;
    private Integer totalInstances;
    private String urls;
    private LocalDateTime lastPushed;
    private String lastEvent;
    private String lastEventActor;
    private String requestedState;
    
    public static String headers() {
        return String.join(",", "organization", "space", 
                "application name", "buildpack", "stack", "running instances", "total instances", "urls", "last pushed", "last event", "last event actor", "requested state");
    }
    
    public static AppDetailBuilder from(AppDetail detail) {
        return AppDetail.builder()
                            .id(detail.getId())
                            .organization(detail.getOrganization())
                            .space(detail.getSpace())
                            .appName(detail.getAppName())
                            .buildpack(detail.getBuildpack())
                            .stack(detail.getStack())
                            .runningInstances(detail.getRunningInstances())
                            .totalInstances(detail.getTotalInstances())
                            .urls(detail.getUrls())
                            .lastPushed(detail.getLastPushed())
                            .lastEvent(detail.getLastEvent())
                            .lastEventActor(detail.getLastEventActor())
                            .requestedState(detail.getRequestedState());
    }
    
    public String toCsv() {
        return String
                .join(",", nonNullify(getOrganization()), nonNullify(getSpace()), 
                        nonNullify(getAppName()), getBuildpack(), 
                        nonNullify(getStack()), String.valueOf(getRunningInstances()), 
                        String.valueOf(getTotalInstances()), nonNullify(getUrls()),
                        getLastPushed().toString(), nonNullify(getLastEvent()), 
                        nonNullify(getLastEventActor()), getRequestedState());
    }
    
    private String nonNullify(String value) {
        return value != null ? value : "";
    }
}
