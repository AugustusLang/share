package com.jd.sharding.client.router;
import java.lang.reflect.Method;

import com.jd.sharding.client.exception.RouteException;
import com.jd.sharding.client.support.RouteResult;
public interface Router<T> {
    RouteResult doRoute(T routeCondition) throws RouteException;


}
