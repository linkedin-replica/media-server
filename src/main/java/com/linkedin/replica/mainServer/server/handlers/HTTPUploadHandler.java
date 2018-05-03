package com.linkedin.replica.mainServer.server.handlers;

import com.google.gson.Gson;
import com.linkedin.replica.mainServer.config.Configuration;
import com.linkedin.replica.mainServer.exceptions.MediaServerException;
import com.linkedin.replica.mediaServer.MediaClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.util.LinkedHashMap;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HTTPUploadHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(true);
    private HttpRequest httpRequest;
    private HttpPostRequestDecoder httpDecoder;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            httpRequest = (HttpRequest) httpObject;
            if (httpRequest.method() == POST) {
                String token = httpRequest.headers().get("access-token");
                String secretKey = Configuration.getInstance().getAppConfigProp("secret.key");
                if(token == null || !validateToken(token, secretKey))
                        throw new MediaServerException("Failed to validate token");
                httpDecoder = new HttpPostRequestDecoder(factory, httpRequest);
                httpDecoder.setDiscardThreshold(0);
            } else
                sendResponse(ctx, METHOD_NOT_ALLOWED, "error","Method not allowed");

        }

        if (httpDecoder != null) {
            if (httpObject instanceof HttpContent) {
                final HttpContent chunk = (HttpContent) httpObject;
                httpDecoder.offer(chunk);
                readChunk(ctx);

                if (chunk instanceof LastHttpContent) {
                    resetPostRequestDecoder();
                }
            }
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

    private void readChunk(ChannelHandlerContext ctx) throws IOException, URISyntaxException {
        while (httpDecoder.hasNext()) {
            InterfaceHttpData data = httpDecoder.next();
            if (data != null) {
                try {
                    switch (data.getHttpDataType()) {
                        case Attribute:
                            break;
                        case FileUpload:
                            final FileUpload fileUpload = (FileUpload) data;
                            String url =  MediaClient.writeFile(fileUpload.getFile());
                            sendResponse(ctx, CREATED, "url", url);
                            break;
                    }
                } finally {
                    data.release();
                }
            }
        }
    }

    private static void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String keyRespond, String valueRespond) {
        LinkedHashMap<String, Object> responseBody = new LinkedHashMap<String, Object>();
        responseBody.put(keyRespond, valueRespond);
        String json = new Gson().toJson(responseBody);

        ByteBuf content = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void resetPostRequestDecoder() {
        httpRequest = null;
        httpDecoder.destroy();
        httpDecoder = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Got exception " + cause.getMessage());
        HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        if (cause instanceof InvalidPathException)
            status = HttpResponseStatus.NOT_FOUND;
        else if (cause instanceof MediaServerException)
            status = HttpResponseStatus.UNAUTHORIZED;
        sendResponse(ctx, status,"error", cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (httpDecoder != null) {
            httpDecoder.cleanFiles();
        }
    }
}
