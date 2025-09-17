package io.netty.channel;

import io.netty.channel.local.LocalChannel;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author fengyang
 * @date 2025-08-20 15:10:59
 * @description
 */
public class DummyChannelHandlerContext extends AbstractChannelHandlerContext {
    public static ChannelHandlerContext DUMMY_INSTANCE = new DummyChannelHandlerContext(
            new DefaultChannelPipeline(new LocalChannel()),
            null,
            "DummyChannelHandlerContext",
            DefaultChannelPipeline.HeadContext.class
    );
    public DummyChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor,
                                      String name, Class<? extends ChannelHandler> handlerClass) {
        super(pipeline, executor, name, handlerClass);
    }

    @Override
    public ChannelHandler handler() {
        return null;
    }
}