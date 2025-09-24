package com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes;

import org.junit.Test;

import static org.junit.Assert.*;

public class BytesWriteTest {

    @Test
    public void testCopy() {
        byte[] source = new byte[10];

        for (int i = 0; i < 10; i++) {
            source[i] = (byte) i;
        }

        System.out.println(source);

        byte[] newBytes = BytesWrite.copy(6, source, 0, 5);
        System.out.println(newBytes);
    }
}