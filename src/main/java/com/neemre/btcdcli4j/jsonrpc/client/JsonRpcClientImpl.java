package com.neemre.btcdcli4j.jsonrpc.client;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.http.client.HttpClient;

import com.neemre.btcdcli4j.http.client.SimpleHttpClient;
import com.neemre.btcdcli4j.http.client.SimpleHttpClientImpl;
import com.neemre.btcdcli4j.jsonrpc.JsonMapper;
import com.neemre.btcdcli4j.jsonrpc.JsonParser;
import com.neemre.btcdcli4j.jsonrpc.domain.JsonRpcError;
import com.neemre.btcdcli4j.jsonrpc.domain.JsonRpcRequest;
import com.neemre.btcdcli4j.jsonrpc.domain.JsonRpcResponse;

public class JsonRpcClientImpl implements JsonRpcClient {
	
	private SimpleHttpClient httpClient;
	private JsonParser parser;
	private JsonMapper mapper;
	
	
	public JsonRpcClientImpl(HttpClient rawHttpClient, Properties nodeConfig) {
		httpClient = new SimpleHttpClientImpl(rawHttpClient, nodeConfig);
		parser = new JsonParser();
		mapper = new JsonMapper();
	}
	
	@Override
	public String execute(String method) {
		return execute(method, null);
	}
	
	@Override
	public <T> String execute(String method, T param) {
		List<T> params = new ArrayList<T>();
		params.add(param);
		return execute(method, params);
	}

	@Override
	public <T> String execute(String method, List<T> params) {
		String requestUuid = getNewUuid();
		JsonRpcRequest<T> request = getNewRequest(method, params, requestUuid);
		String responseJson = httpClient.execute(mapper.mapToJson(request));
		JsonRpcResponse response = mapper.mapToEntity(responseJson, JsonRpcResponse.class);
		response = verifyResponse(request, response);
		response = checkResponse(request, response);
		return response.getResult();
	}
	
	@Override
	public JsonParser getParser() {
		return parser;
	}
	
	@Override
	public JsonMapper getMapper() {
		return mapper;
	}
	
	private <T> JsonRpcRequest<T> getNewRequest(String method, List<T> params, String id) {
		JsonRpcRequest<T> rpcRequest = new JsonRpcRequest<T>();
		rpcRequest.setMethod(method);
		rpcRequest.setParams(params);
		rpcRequest.setId(id);
		return rpcRequest;
	}
	
	private JsonRpcResponse getNewResponse(String result, JsonRpcError error, String id) {
		JsonRpcResponse rpcResponse = new JsonRpcResponse();
		rpcResponse.setResult(result);
		rpcResponse.setError(error);
		rpcResponse.setId(id);
		return rpcResponse;
	}
	
	private String getNewUuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private <T> JsonRpcResponse verifyResponse(JsonRpcRequest<T> request, JsonRpcResponse response) {
		if(!response.getId().equals(request.getId())) {
			System.out.printf("%s.%s(..): I am broken.", getClass().getSimpleName(), 
					getClass().getEnclosingMethod().getName());	//TODO
		}
		return response;
	}
	
	private <T> JsonRpcResponse checkResponse(JsonRpcRequest<T> request, JsonRpcResponse response) {
		if(!response.getId().equals(request.getId())) {
			if(!response.getError().equals(null)) {
				System.out.printf("%s.%s(..): I am broken.", getClass().getSimpleName(), 
						getClass().getEnclosingMethod().getName());	//TODO
			}
		}
		return response;
	}
}