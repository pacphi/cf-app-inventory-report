package io.pivotal.cfapp.domain;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

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
                .join(",", wrap(getOrganization()), wrap(getSpace()), 
                        wrap(getAppName()), wrap(getBuildpack()), 
                        wrap(getStack()), wrap(String.valueOf(getRunningInstances())), 
                        wrap(String.valueOf(getTotalInstances())), wrap(getUrls()),
                        wrap(getLastPushed() != null ? getLastPushed().toString(): ""), wrap(getLastEvent()), 
                        wrap(getLastEventActor()), wrap(getRequestedState()));
    }
    
    private String wrap(String value) {
        return value != null ? StringUtils.wrap(value, '"') : StringUtils.wrap("", '"');
    }
}
