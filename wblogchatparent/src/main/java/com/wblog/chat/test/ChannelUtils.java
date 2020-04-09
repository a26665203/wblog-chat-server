package com.wblog.chat.test;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class ChannelUtils {
    public static ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

}
