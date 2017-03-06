package com.jd.sharding.client.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class ShardConfig {
     private Map<String, DbConfig> tableConfig;

    public Map<String, DbConfig> getTableConfig() {
        if(CollectionUtils.isEmpty(tableConfig)) {
              tableConfig = Collections.emptyMap();
        }
        return Collections.unmodifiableMap(tableConfig);
    }

    /**
     * setter for the field:tableConfig
     * @param tableConfig
     */
    public void setTableConfig(Map<String, DbConfig> tableConfig) {
        this.tableConfig = tableConfig;
    }
}
