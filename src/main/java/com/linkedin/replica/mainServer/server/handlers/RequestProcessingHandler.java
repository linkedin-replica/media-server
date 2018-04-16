package com.linkedin.replica.mainServer.server.handlers;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.linkedin.replica.mainServer.config.Configuration;
import com.linkedin.replica.mainServer.model.Request;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;


public class RequestProcessingHandler extends ChannelInboundHandlerAdapter {
	private ChannelHandlerContext ctx;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) 
			throws Exception{	
		Request request = (Request) msg;
		JsonObject json;
		
		if(request.getRequestURI().equals(Configuration.getInstance().getAppConfigProp("health.endpoint"))){
			json = new JsonObject();
			json.addProperty("statusCode", 200);
			ctx.writeAndFlush(json);
		}else{
			
//			final String corrId = UUID.randomUUID().toString();
//			ResponseMessageReceiver.getInstance().addListener(corrId, this);
//			this.ctx = ctx;
//			String serviceName = Configuration.getInstance().getWebServConfigProp(request.getWebServName());
//			String queueName = Configuration.getInstance().getWebServConfigProp(request.getWebServName()+".queue");
//			Channel channel = MessageQueueConnection.getInstance().newChannel(serviceName);
//
//	        // Create the response message properties
//	        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
//	                .Builder()
//	                .correlationId(corrId)
//	                .replyTo(Configuration.getInstance().getAppConfigProp("rabbitmq.queue.name"))
//	                .build();

	        json =  parse(request.getBody(), request.getQueryParams());
	        if(request.getUserId() != null)
	        	json.addProperty("userId",request.getUserId());
	        
	        if(request.getFuncName() == null)
	        	json.addProperty("commandName", Configuration.getInstance().getCommandConfigProp(request.getWebServName()));
	        else
			{
				json.addProperty("commandName", Configuration.getInstance().getCommandConfigProp(request.getWebServName()+"."+request.getFuncName()));
				
			}


	        // public message to queue
//	        channel.basicPublish("",queueName, replyProps, json.toString().getBytes());
//	        channel.close();


        }
	}

	
	/**
	 * Overriding exceptionCaught()  to react to any Throwable.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		JsonObject json = new JsonObject();
		if(cause instanceof JsonParseException || cause instanceof JsonSyntaxException){
			json.addProperty("statusCode", HttpResponseStatus.BAD_REQUEST.code());
			json.addProperty("errMessage", "Invalid Json body.");
		}else{
			json.addProperty("statusCode", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
			json.addProperty("errMessage", cause.getMessage());
			cause.printStackTrace();
		}
		
		ctx.writeAndFlush(json);
	}

	public void onResponse(String response) {
		ctx.writeAndFlush(response);
	}
	
	private JsonObject parse(String body, String queryStr){
		JsonObject messageParams = new JsonObject();
		
		if(queryStr != null){
			String[] params = queryStr.split("&");
			String[] keyVal;
			
			for(String param : params){
				// eg. param = userId="Ahmed"
				keyVal = param.split("=");
				messageParams.addProperty(keyVal[0], keyVal[1].replace("\"", ""));
			}
		}
		
		if(body != null && !body.trim().isEmpty()){
			JsonObject jsonBody = new JsonParser().parse(body).getAsJsonObject();
			for(String key: jsonBody.keySet())
				messageParams.add(key, jsonBody.get(key));
		}
		
		return messageParams;
	}
}