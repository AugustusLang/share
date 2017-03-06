package com.jd.sharding.client.datasource;

import java.util.Map;

/**
 *
 *
 * @author:huangzongwang
 * @since: 1.0.0
 */
public interface MultipleDataSourcesService {

    /**
     * 获取所有的数据源
     *
     * @return 所有的数据源的不可变集合
     */
    Map<String, String> getDataSources();
}
