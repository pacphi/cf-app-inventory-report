package io.pivotal.cfapp.domain;

import lombok.Data;

@Data
public class BuildpackCount {
    
    private String buildpack;
    private long total;
    
    public String toCsv() {
        return String.join(",", buildpack, String.valueOf(total));
    }
    
    public static String headers() {
        return String.join(",", "buildpack", "total");
    }
}
