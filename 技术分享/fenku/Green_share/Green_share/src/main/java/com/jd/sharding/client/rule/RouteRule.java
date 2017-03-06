package com.jd.sharding.client.rule;
public interface RouteRule<T> {
    int getDbIndex(T routeFactor);

}
