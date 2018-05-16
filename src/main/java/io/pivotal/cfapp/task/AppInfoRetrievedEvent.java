package io.pivotal.cfapp.task;

import java.util.List;

import org.springframework.context.ApplicationEvent;

public class AppInfoRetrievedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private List<AppInfo> info;
    
    public AppInfoRetrievedEvent(Object source, List<AppInfo> info) {
        super(source);
        this.info = info;
    }
    
    public List<AppInfo> getInfo() {
        return info;
    }

}
