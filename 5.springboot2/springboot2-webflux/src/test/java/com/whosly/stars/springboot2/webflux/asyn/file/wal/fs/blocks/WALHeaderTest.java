package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks;

import org.junit.Assert;
import org.junit.Test;

public class WALHeaderTest {

    @Test
    public void testToBytes() {
        WALHeader header = new WALHeader((byte)1, 6L, "wal-yyyyMMddHHmmss-dd");
        byte[] headBytes = header.toBytes();

        WALHeader walHeader = WALHeader.fromBytes(headBytes);

        Assert.assertEquals(header.getCreateAt(), walHeader.getCreateAt());
        Assert.assertEquals(header.getType(), walHeader.getType());
        Assert.assertEquals(header.getStartLsn(), walHeader.getStartLsn());
        Assert.assertEquals(header.getLength(), walHeader.getLength());
    }

}