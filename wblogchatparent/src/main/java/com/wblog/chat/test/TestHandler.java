package com.wblog.chat.test;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestHandler extends ChannelInboundHandlerAdapter {
    private final String wsUrl = "/ws";
    private final String connectReq = "connect";
    private final String chatReq = "chat";
    private WebSocketServerHandshaker handshaker;
    @Override
    public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception{
        if(var2 instanceof FullHttpRequest){
            System.out.println("介绍到了http请求");
            handleHttpRequest(var1, (FullHttpRequest) var2);
        }else if(var2 instanceof WebSocketFrame){
            handlerWebSocketRequest(var1, (WebSocketFrame) var2);
        }

    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){
        if(fullHttpRequest instanceof HttpRequest){
            System.out.println("请求体"+fullHttpRequest.toString());
            HttpMethod method = fullHttpRequest.method();
            System.out.println("uri是----->"+fullHttpRequest.uri());
            if(wsUrl.equalsIgnoreCase(fullHttpRequest.uri())){
                WebSocketServerHandshakerFactory webSocketServerHandshakerFactory =
                        new WebSocketServerHandshakerFactory("",null,false);
                handshaker = webSocketServerHandshakerFactory.newHandshaker(fullHttpRequest);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), fullHttpRequest);
                }
            }
        }
    }
    private void handlerWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame){
        // 判断是否是关闭链路的指令
        System.out.println("websocket get");
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame)webSocketFrame.retain());
            return;
        }
        // 判断是否是Ping消息
        if (webSocketFrame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }
        // 文本消息，不支持二进制消息
        if (webSocketFrame instanceof TextWebSocketFrame) {
            // 返回应答消息
            String requestmsg = ((TextWebSocketFrame) webSocketFrame).text();
            System.out.println("websocket消息======"+requestmsg);
            //消息格式：reqType@#senderNickName@#receiverNickName@#message
            String[] mid = requestmsg.split("@#");
            if(connectReq.equals(mid[0])){
                ChannelUtils.channels.put(mid[1],ctx.channel());
                System.out.println(mid[1]+"登陆了");
                return;
            }else if(chatReq.equals(mid[0])){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                Channel channel = ChannelUtils.channels.get(mid[2]);
                if(channel == null){
                    channel = ChannelUtils.channels.get(mid[1]);
                    channel.writeAndFlush(new TextWebSocketFrame(mid[2]+"@#"+"[系统提示]该用户不在线--->"+simpleDateFormat.format(date)));
                    return;
                }
                channel.writeAndFlush(new TextWebSocketFrame(mid[1]+"@#"+mid[3]+"--->"+simpleDateFormat.format(date)));
            }
            System.out.println("消息是:"+mid[3]);
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            // 判断Channel是否读空闲, 读空闲时移除Channel
            if (evnet.state().equals(IdleState.READER_IDLE)) {

            }
        }
        ctx.fireUserEventTriggered(evt);
    }
}
