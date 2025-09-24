package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks;

import org.junit.Assert;
import org.junit.Test;

public class WALTrailerTest {

    @Test
    public void testToBytes() {
        WALTrailer trailer = new WALTrailer("wal-yyyyMMddHHmmss-dd");

        byte[] bytes = trailer.toBytes();

        WALTrailer walTrailer = WALTrailer.fromBytes(bytes);
        Assert.assertEquals(trailer.getFlag(), walTrailer.getFlag());
    }

}