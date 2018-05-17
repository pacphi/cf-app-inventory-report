package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import io.pivotal.cfapp.domain.AppDetail;
import io.pivotal.cfapp.domain.BuildpackCount;

public class AppInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<AppDetail> detail;
    private List<BuildpackCount> buildpackCounts;;
    
    public AppInfoRetrievedEvent(Object source, List<AppDetail> detail, List<BuildpackCount> buildpackCounts) {
        super(source);
        this.detail = detail;
        this.buildpackCounts = buildpackCounts;
    }
    
    public List<AppDetail> getDetail() {
        return detail;
    }

    public List<BuildpackCount> getBuildpackCounts() {
        return buildpackCounts;
    }
}
