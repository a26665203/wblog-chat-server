package com.wblog.chat.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyTestServer {
    private static Channel channel;
    public static void main(String[] args){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap  = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).
                childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("http-codec", new HttpServerCodec());
                        socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                        socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                        socketChannel.pipeline().addLast(new TestHandler());
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        try{
            ChannelFuture future = bootstrap.bind(8085).sync();
            System.out.println("服务器启动了");
            channel = future.channel();
            future.channel().closeFuture().sync();

        }catch (Exception e){
            
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            channel.closeFuture().syncUninterruptibly();
        }
    }
}
