package io.pivotal.cfapp.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;

@Component
public class AppDetailAggregator {

    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public AppDetailAggregator(ReactiveMongoTemplate reactiveMongoTemplate) {
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
    
}
