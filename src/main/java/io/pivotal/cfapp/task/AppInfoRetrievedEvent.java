package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;

public class AppInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final List<AppDetail> detail;
    private final List<BuildpackCount> buildpackCounts;
    private final List<OrganizationCount> organizationCounts;
    private final List<DockerImageCount> dockerImages;
    
    public AppInfoRetrievedEvent(
            Object source, 
            List<AppDetail> detail, 
            List<BuildpackCount> buildpackCounts,
            List<OrganizationCount> organizationCounts,
            List<DockerImageCount> dockerImages
            ) {
        super(source);
        this.detail = detail;
        this.buildpackCounts = buildpackCounts;
        this.organizationCounts = organizationCounts;
        this.dockerImages = dockerImages;
    }
    
    public List<AppDetail> getDetail() {
        return detail;
    }

    public List<BuildpackCount> getBuildpackCounts() {
        return buildpackCounts;
    }
    
    public List<OrganizationCount> getOrganizationCounts() {
        return organizationCounts;
    }
    
    public List<DockerImageCount> getDockerImages() {
    	return dockerImages;
    }
    
}
