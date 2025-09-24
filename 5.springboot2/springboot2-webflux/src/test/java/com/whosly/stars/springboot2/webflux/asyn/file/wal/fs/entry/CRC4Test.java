package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

import junit.framework.TestCase;

public class CRC4Test extends TestCase {

    public void testGetCRC() {
        RecordBloEntry entry = new RecordBloEntry("p1", Long.MAX_VALUE,
                "create table SaleOrder\n" +
                        "(\n" +
                        "    id 　　　　　　int identity(1,1),\n");

        byte[] body = entry.toBytes();

        RecordBloHeader header = new RecordBloHeader(
                new RecordHeaderData(1, 2022_1231_235959_66666L), body
        );
        byte[] bys = header.toBytes();

        RecordBloHeader header1 = RecordBloHeader.fromBytes(bys);
        header1.getCrc();
    }
}