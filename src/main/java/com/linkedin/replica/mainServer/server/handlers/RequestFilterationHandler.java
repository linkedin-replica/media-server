package com.linkedin.replica.mainServer.server.handlers;

import java.io.UnsupportedEncodingException;
import java.nio.file.InvalidPathException;
import java.util.Date;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.linkedin.replica.mainServer.config.Configuration;
import com.linkedin.replica.mainServer.exceptions.MainServerException;
import com.linkedin.replica.mainServer.model.Request;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class RequestFilterationHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Request request = (Request) msg;
		if(!request.getRequestURI().equals(Configuration.getInstance().getAppConfigProp("health.endpoint")))
			System.out.printf("[%s][%s] %s\n", new Date().toString(), request.getMethod().name(), request.getRequestURI());
		// check if request URI is not a valid web service call
		if (request.getRequestURI()
				.equals(Configuration.getInstance().getAppConfigProp(
						"health.endpoint"))) {
			ctx.fireChannelRead(request);

		} else {
			if (request.getWebServName() == null
					|| request.getFuncName() == null
					|| Configuration.getInstance().getWebServConfigProp(
							request.getWebServName()) == null
					|| Configuration.getInstance().getCommandConfigProp(
							request.getWebServName() + "."
									+ request.getFuncName()) == null)
				throw new InvalidPathException(request.getRequestURI(),
						"Access Denied, forbidden request");

			// check that POST, PUT and DELETE requests have a valid body
			HttpMethod method = request.getMethod();
			if ((method.equals(HttpMethod.POST)
					|| method.equals(HttpMethod.PUT) || method
						.equals(HttpMethod.DELETE))
					&& (request.getBody().isEmpty() || request.getBody()
							.replaceAll(" ", "").isEmpty()))
				throw new MainServerException("Request Body must not be empty.");

			String token = request.getHeaders().get("access-token");

			// Validate and extract user Id

			String secretKey = Configuration.getInstance().getAppConfigProp("secret.key");
			if(token != null) {
				if(validateToken(token, secretKey))
					request.setUserId(getClaims(token, secretKey).getBody().getId());
				else
					throw new MainServerException("Failed to validate token");
			}
			ctx.fireChannelRead(request);
		}
	}

	/**
	 * Get claims (stored data) from a valid token
	 * @param token
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static Jws<Claims> getClaims(String token, String secretKey) throws UnsupportedEncodingException {
		return Jwts.parser()
				.setSigningKey(secretKey.getBytes("UTF-8"))
				.parseClaimsJws(token);
	}


	/**
	 * Validate jwt token
	 *
	 * @param token jwt token to be authenticated
	 * @return Weather
	 */

	private static boolean validateToken(String token, String secretKey) {
		try {
			getClaims(token, secretKey);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Overriding exceptionCaught() to react to any Throwable.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// construct Error Response
		LinkedHashMap<String, Object> responseBody = new LinkedHashMap<String, Object>();

		// set Http status code
		if (cause instanceof InvalidPathException) {
			responseBody.put("statusCode", HttpResponseStatus.NOT_FOUND.code());
		} else {
			if (cause instanceof MainServerException) {
				responseBody.put("statusCode",
						HttpResponseStatus.BAD_REQUEST.code());
			} else {
				responseBody.put("statusCode", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
				cause.printStackTrace();
			}
		}
		responseBody.put("errMessage", cause.getMessage());
		String json = new Gson().toJson(responseBody);
		// send response to ResponseEncoderHandler
		ctx.writeAndFlush(json);
	}
}