package com.yueny.study.netty.chats.client;

import com.yueny.study.netty.core.socket.client.AbstractChatClientHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author fengyang
 * @date 2025-08-18 17:02:58
 * @description
 */
public class ChatsClientHandler extends AbstractChatClientHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println(s.trim());
    }
}