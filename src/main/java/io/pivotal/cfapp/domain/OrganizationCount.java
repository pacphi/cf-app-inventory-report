package io.pivotal.cfapp.domain;

import lombok.Data;

@Data
public class OrganizationCount {
    
    private String organization;
    private long total;
    
    public String toCsv() {
        return String.join(",", organization, String.valueOf(total));
    }
    
    public static String headers() {
        return String.join(",", "organization", "total");
    }
}
