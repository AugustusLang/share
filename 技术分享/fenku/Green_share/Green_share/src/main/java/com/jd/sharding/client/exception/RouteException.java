package com.jd.sharding.client.exception;

import org.springframework.dao.DataAccessException;
public class RouteException extends DataAccessException {



	/**
	 * 
	 */
	private static final long serialVersionUID = 7201101900545005878L;

	public RouteException(String message) {
        super(message);
    }

    public RouteException(String message, Throwable cause) {
        super(message, cause);
    }
}
