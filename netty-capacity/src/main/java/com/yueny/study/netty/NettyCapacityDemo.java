package com.yueny.study.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

/**
 * @author fengyang
 * @date 2023/6/30 上午11:18
 * @see <a href="fengyang@stoneatom.com">mailTo: fengyang@stoneatom.com</a>
 */
public class NettyCapacityDemo {
    public static void main(String[] args) {
        // 1.创建一个非池化的ByteBuf，大小为10个字节
        ByteBuf buf = Unpooled.buffer(30);
        System.out.println("创建一个非池化的ByteBuf, 大小为10个字节");
        System.out.println("原始ByteBuf为====================>"+buf.toString());
        System.out.println("1.ByteBuf中的内容为===============>"+ Arrays.toString(buf.array())+"\n");

        // 2.写入一段内容
        System.out.println("写入20");
        byte[] bytes = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        buf.writeBytes(bytes);
        System.out.println("写入的bytes为====================>"+Arrays.toString(bytes));
        System.out.println("写入一段内容后ByteBuf为===========>"+buf.toString());
        System.out.println("2.ByteBuf中的内容为===============>"+Arrays.toString(buf.array())+"\n");

        // 3.读取一段内容
        System.out.println("读取10");
        byte b1 = buf.readByte();
        byte b2 = buf.readByte();
        byte b3 = buf.readByte();
        byte b4 = buf.readByte();
        byte b5 = buf.readByte();
        byte b6 = buf.readByte();
        byte b7 = buf.readByte();
        byte b8 = buf.readByte();
        byte b9 = buf.readByte();
        byte b10 = buf.readByte();
        System.out.println("读取的bytes为====================>"+Arrays.toString(new byte[]{b1,b2,b3,b4,b5,b6,b7,b8,b9,b10}));
        System.out.println("读取一段内容后ByteBuf为===========>"+buf.toString());
        System.out.println("3.ByteBuf中的内容为===============>"+Arrays.toString(buf.array())+"\n");

        // 4.将读取的内容丢弃
        System.out.println("将读取的内容丢弃， discardReadBytes 时出现了内存复制");
        buf.discardReadBytes();
        System.out.println("将读取的内容丢弃后ByteBuf为========>"+buf.toString());
        System.out.println("4.ByteBuf中的内容为===============>"+Arrays.toString(buf.array())+"\n");

        // 5.清空读写指针
        System.out.println("清空读写指针");
        buf.clear();
        System.out.println("将读写指针清空后ByteBuf为==========>"+buf.toString());
        System.out.println("5.ByteBuf中的内容为===============>"+Arrays.toString(buf.array())+"\n");

        // 6.再次写入一段内容，比第一段内容少
        System.out.println("再次写入一段内容，比第一段内容少");
        byte[] bytes2 = {1,2,3};
        buf.writeBytes(bytes2);
        System.out.println("写入的bytes为====================>"+Arrays.toString(bytes2));
        System.out.println("写入一段内容后ByteBuf为===========>"+buf.toString());
        System.out.println("6.ByteBuf中的内容为===============>"+Arrays.toString(buf.array())+"\n");

        // 7.将ByteBuf清零
        System.out.println("将ByteBuf清零");
        buf.setZero(0,buf.capacity());
        System.out.println("将内容清零后ByteBuf为==============>"+buf.toString());
        System.out.println("7.ByteBuf中的内容为================>"+Arrays.toString(buf.array())+"\n");

        // 8.再次写入一段超过容量的内容
        System.out.println("再写，出现扩容");
        byte[] bytes3 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36};
        buf.writeBytes(bytes3);
        System.out.println("写入的bytes为====================>"+Arrays.toString(bytes3));
        System.out.println("写入一段内容后ByteBuf为===========>"+buf.toString());
        System.out.println("8.ByteBuf中的内容为===============>"+Arrays.toString(buf.array())+"\n");
    }

}
