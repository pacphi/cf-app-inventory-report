package io.pivotal.cfapp.repository.mongo;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;
import io.pivotal.cfapp.domain.DockerImageCount;
import io.pivotal.cfapp.domain.OrganizationCount;
import io.pivotal.cfapp.repository.AppDetailAggregator;

@Profile("mongo")
@Component
public class MongoAppDetailAggregator implements AppDetailAggregator {

    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public MongoAppDetailAggregator(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }
    
    public List<BuildpackCount> countApplicationsByBuildpack() {
        Aggregation agg = newAggregation(
            project("buildpack"),
            unwind("buildpack"),
            group("buildpack").count().as("total"),
            project("total").and("buildpack").previousOperation(),
            sort(Sort.Direction.DESC, "total")
        );
        return reactiveMongoTemplate
                .aggregate(agg, AppDetail.class, BuildpackCount.class)
                    .toStream()
                        .collect(Collectors.toList());
    }
    
    public List<OrganizationCount> countApplicationsByOrganization() {
        Aggregation agg = newAggregation(
            project("organization"),
            unwind("organization"),
            group("organization").count().as("total"),
            project("total").and("organization").previousOperation(),
            sort(Sort.Direction.DESC, "total")
        );
        return reactiveMongoTemplate
                .aggregate(agg, AppDetail.class, OrganizationCount.class)
                    .toStream()
                        .collect(Collectors.toList());
    }

	@Override
	public List<DockerImageCount> countApplicationsByDockerImage() {
		Aggregation agg = newAggregation(
	            project("image"),
	            unwind("image"),
	            group("image").count().as("total"),
	            project("total").and("image").previousOperation(),
	            sort(Sort.Direction.DESC, "total")
	        );
	        return reactiveMongoTemplate
	                .aggregate(agg, AppDetail.class, DockerImageCount.class)
	                    .toStream()
	                        .collect(Collectors.toList());
	}
    
}
