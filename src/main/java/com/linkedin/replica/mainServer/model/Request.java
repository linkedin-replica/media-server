package com.linkedin.replica.mainServer.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

public class Request {
	private String requestURI;
	private String webServName;
	private String funcName;
	private HttpHeaders headers;
	private HttpMethod method;
	private String body;
	private String queryParams;
	private String userId;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {

		return userId;
	}
	public String getRequestURI() {
		return requestURI;
	}
	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}
	public String getWebServName() {
		return webServName;
	}
	public void setWebServName(String webServName) {
		this.webServName = webServName;
	}
	public String getFuncName() {
		return funcName;
	}
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}
	public HttpHeaders getHeaders() {
		return headers;
	}
	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}
	public HttpMethod getMethod() {
		return method;
	}
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getQueryParams() {
		return queryParams;
	}
	public void setQueryParams(String queryParams) {
		this.queryParams = queryParams;
	}
}