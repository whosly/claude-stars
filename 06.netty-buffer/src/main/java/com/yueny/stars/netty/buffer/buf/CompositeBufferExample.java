package com.yueny.stars.netty.buffer.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author fengyang
 * @date 2025-08-20 16:51:50
 * @description
 */
public class CompositeBufferExample {
    public static void main(String[] args) {
        compositeBuffer();
    }

    public static void compositeBuffer() {
        Iterator<ByteBuf> it = getByteBuf().iterator();
        // 访问CompositeByteBuf数据方式一：使用迭代器的方式，逐个访问 ByteBuf
        while(it.hasNext()){
            ByteBuf buf = it.next();
            while (buf.isReadable()){
                System.out.print((char) buf.readByte());
            }
        }
        System.out.println("");

        // 访问CompositeByteBuf数据方式二：使用迭代器的方式，逐个访问 ByteBuf
        CompositeByteBuf messageBuf = getByteBuf();
        for (ByteBuf buf : messageBuf) {
            System.out.print(buf.toString(Charset.forName("UTF-8")));
        }
        System.out.println("");

        // 访问CompositeByteBuf数据方式三：使用数组访问数据
        messageBuf = getByteBuf();
        if(!messageBuf.hasArray()){
            int len = messageBuf.readableBytes();
            byte[] arr = new byte[len];
            messageBuf.getBytes(0, arr);
            for (byte b : arr){
                System.out.print((char)b);
            }
        }
        System.out.println("");

        //删除位于索引位置为 0（第一个组件）的 ByteBuf
        messageBuf.removeComponent(0);
    }

    private static CompositeByteBuf getByteBuf() {
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        ByteBuf headBuf = Unpooled.copiedBuffer("Hello,", CharsetUtil.UTF_8);
        ByteBuf bodyBuf = Unpooled.copiedBuffer("Netty!", CharsetUtil.UTF_8);

        //将 ByteBuf 实例追加到 CompositeByteBuf. 在添加组件时同时推进 writerIndex
        messageBuf.addComponents(true, headBuf, bodyBuf);

        return messageBuf;
    }
}
