package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.block;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordBloEntry;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordHeaderBuilder;
import junit.framework.TestCase;
import org.junit.Assert;

public class WALBlockLogTest extends TestCase {

    public void testToBytes() {
        WALBlockLog walFileLog = new WALBlockLog(
                RecordHeaderBuilder.build(1, Long.MAX_VALUE),
                new RecordBloEntry("p2", 66L,
                        "begin\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "end\n")
        );

        byte[] bytes = walFileLog.toBytes();

        WALBlockLog fileLog = WALBlockLog.fromBytes(bytes);

        Assert.assertEquals(walFileLog.getBody().getSql(), fileLog.getBody().getSql());
        Assert.assertEquals(walFileLog.getHeader().getCrc(), fileLog.getHeader().getCrc());
    }

    public void testFromBytes() {
    }
}