package com.huatu.ztk.scm.agent;

import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class AgentConfiguration extends Configuration {

    @NotEmpty
    private String defaultName = "agent";

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }
}

