package io.pivotal.cfapp.task;

public class AppInfo {

    private String organization;
    private String space;
    private String appName;
    private String buildpack;
    private String instances;
    private String url;
    
    public AppInfo(
            String organization, String space, 
            String appName, String buildpack, 
            String instances, String url) {
        this.organization = organization;
        this.space = space;
        this.appName = appName;
        this.buildpack = buildpack;
        this.instances = instances;
        this.url = url;
    }

    public String getOrganization() {
        return organization != null ? organization: "";
    }

    public String getSpace() {
        return space != null ? space: "";
    }

    public String getAppName() {
        return appName!= null ? appName: "";
    }

    public String getBuildpack() {
        return buildpack != null ? buildpack : "";
    }
    
    public String getInstances() {
        return instances != null ? instances: "";
    }
    
    public String getUrl() {
        return url != null ? url : "";
    }
    
    public static String headers() {
        return String.join(",", "organization", "space", "appName", "buildpack", "instances (running/total)", "url");
    }
    
    public String toString() {
        return String.join(",", getOrganization(), getSpace(), getAppName(), getBuildpack(), getInstances(), getUrl());
    }
    
}
