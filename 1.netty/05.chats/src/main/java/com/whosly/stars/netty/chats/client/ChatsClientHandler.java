package com.whosly.stars.netty.chats.client;

import com.whosly.stars.netty.chats.Message;
import com.whosly.stars.netty.core.socket.client.AbstractChatClientHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author fengyang
 * @date 2025-08-18 17:02:58
 * @description
 */
class ChatsClientHandler extends AbstractChatClientHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
        System.out.println(msg.getMessage());
    }
}