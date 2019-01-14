package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;

public class AppDetailRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<AppDetail> detail;
    private List<BuildpackCount> buildpackCounts;
    private List<OrganizationCount> organizationCounts;
    private List<DockerImageCount> dockerImages;

    public AppDetailRetrievedEvent(Object source) {
        super(source);
    }

    public AppDetailRetrievedEvent detail(List<AppDetail> detail) {
        this.detail = detail;
        return this;
    }

    public AppDetailRetrievedEvent buildpackCounts(List<BuildpackCount> buildpackCounts) {
        this.buildpackCounts = buildpackCounts;
        return this;
    }

    public AppDetailRetrievedEvent organizationCounts(List<OrganizationCount> organizationCounts) {
        this.organizationCounts = organizationCounts;
        return this;
    }

    public AppDetailRetrievedEvent dockerImages(List<DockerImageCount> dockerImages) {
        this.dockerImages = dockerImages;
        return this;
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
