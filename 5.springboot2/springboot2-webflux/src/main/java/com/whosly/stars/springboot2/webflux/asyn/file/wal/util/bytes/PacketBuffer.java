package com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes;

import java.nio.ByteBuffer;

public interface PacketBuffer {

    ByteBuffer toByteBuffer();

    byte readByte();

    byte readByte(int postion);

    int readBytes(byte[] ab, int offset, int len);

    void writeByte(byte bte);

    int writeBytes(byte[] btes);

    int getPacketLength();

    int getPosition();

    void setPosition(int positionToSet);

    void reset();

    int remaining();

}
