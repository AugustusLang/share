package com.jd.sharding.client.annotation;

import java.util.Map;

public class ShardResult {

	private boolean isSuccess;
	private int errorCode;
	private String errorDesc;
	private Map<String, Object> resultParam;
	
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDesc() {
		return errorDesc;
	}
	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}
	public Map<String, Object> getResultParam() {
		return resultParam;
	}
	public void setResultParam(Map<String, Object> resultParam) {
		this.resultParam = resultParam;
	}
	
	
}
