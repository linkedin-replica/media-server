package com.linkedin.replica.mainServer.server;

import com.linkedin.replica.mainServer.server.handlers.HTTPServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void start(String ip, int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HTTPServerInitializer());
            InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
            //TODO:: change it to socketAddress later
            Channel ch = b.bind(port).sync().channel();
            System.out.println("Server is listening on http://" + ip + ":" + port + '/');
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
