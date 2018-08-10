package io.pivotal.cfapp.repository;

import java.util.List;

import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;

public interface AppDetailAggregator {
    
    public List<BuildpackCount> countApplicationsByBuildpack();
    public List<OrganizationCount> countApplicationsByOrganization();
	public List<DockerImageCount> countApplicationsByDockerImage();
    
}
