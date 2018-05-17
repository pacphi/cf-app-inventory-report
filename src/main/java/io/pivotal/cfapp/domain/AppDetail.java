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
    private String requestedState;
    
    public static String headers() {
        return String.join(",", "organization", "space", 
                "application name", "buildpack", "stack", "running instances", "total instances", "urls", "last pushed", "current state");
    }
    
    public String toCsv() {
        return String
                .join(",", nonNullify(getOrganization()), nonNullify(getSpace()), 
                        nonNullify(getAppName()), getBuildpack(), 
                        nonNullify(getStack()), String.valueOf(getRunningInstances()), 
                        String.valueOf(getTotalInstances()), nonNullify(getUrls()),
                        getLastPushed().toString(), getRequestedState());
    }
    
    private String nonNullify(String value) {
        return value != null ? value : "";
    }
}
