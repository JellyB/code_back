package com.arj.monitor.conf.druid;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhouwei
 */
public class MultiDataSources {
    private Map<String,DataSource> datasources;

    public MultiDataSources(Map<String,DataSource> datasources){
        this.datasources = Collections.unmodifiableMap(datasources);
    }

    public Map<String, DataSource> getDatasources() {
        return datasources;
    }
}
