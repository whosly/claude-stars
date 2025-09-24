package com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry;

import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;

public class RecordEntryMain {
    public static void main(String[] args) {
        int len = 9;
        long slen = 0x1000000L;
        System.out.println("slen:" + slen);

        RecordBloEntry entry = new RecordBloEntry("p1", Long.MAX_VALUE,
                "create table SaleOrder\n" +
                        "(\n" +
                        "    id 　　　　　　int identity(1,1),\n" +
                        "    OrderNumber  int　　　　　　　　 ,\n" +
                        "    CustomerId   varchar(20)      ,\n" +
                        "    OrderDate    datetime         ,\n" +
                        "    Remark       varchar(200)\n" +
                        ")\n" +
                        "while @i<100000\n" +
                        "begin\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "end\n" +
                        "create index idx_OrderNumber on SaleOrder(OrderNumber)\n" +
                        "create index idx_CustomerId on SaleOrder(CustomerId)\n" +
                        "(\n" +
                        "    id 　　　　　　int identity(1,1),\n" +
                        "    OrderNumber  int　　　　　　　　 ,\n" +
                        "    CustomerId   varchar(20)      ,\n" +
                        "    OrderDate    datetime         ,\n" +
                        "    Remark       varchar(200)\n" +
                        ")\n" +
                        "while @i<100000\n" +
                        "begin\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "end\n" +
                        "create index idx_OrderNumber on SaleOrder(OrderNumber)\n" +
                        "create index idx_CustomerId on SaleOrder(CustomerId)\n" +
                        "(\n" +
                        "    id 　　　　　　int identity(1,1),\n" +
                        "    OrderNumber  int　　　　　　　　 ,\n" +
                        "    CustomerId   varchar(20)      ,\n" +
                        "    OrderDate    datetime         ,\n" +
                        "    Remark       varchar(200)\n" +
                        ")\n" +
                        "while @i<100000\n" +
                        "begin\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "end\n" +
                        "create index idx_OrderNumber on SaleOrder(OrderNumber)\n" +
                        "create index idx_CustomerId on SaleOrder(CustomerId)\n" +
                        "(\n" +
                        "    id 　　　　　　int identity(1,1),\n" +
                        "    OrderNumber  int　　　　　　　　 ,\n" +
                        "    CustomerId   varchar(20)      ,\n" +
                        "    OrderDate    datetime         ,\n" +
                        "    Remark       varchar(200)\n" +
                        ")\n" +
                        "while @i<100000\n" +
                        "begin\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "end\n" +
                        "create index idx_OrderNumber on SaleOrder(OrderNumber)\n" +
                        "create index idx_CustomerId on SaleOrder(CustomerId)\n" +
                        "(\n" +
                        "    id 　　　　　　int identity(1,1),\n" +
                        "    OrderNumber  int　　　　　　　　 ,\n" +
                        "    CustomerId   varchar(20)      ,\n" +
                        "    OrderDate    datetime         ,\n" +
                        "    Remark       varchar(200)\n" +
                        ")\n" +
                        "while @i<100000\n" +
                        "begin\n" +
                        "    insert into SaleOrder values (@i,CONCAT('C',cast(RAND()*1000 as int)),GETDATE()-RAND()*100,NEWID())\n" +
                        "    set @i=@i+1\n" +
                        "end\n" +
                        "create index idx_OrderNumber on SaleOrder(OrderNumber)\n" +
                        "create index idx_CustomerId on SaleOrder(CustomerId)\n" +
                        "create index idx_OrderDate on SaleOrder(OrderDate)\n");
        System.out.println("entry:" + entry);
        byte[] toBytes = entry.toBytes();
        System.out.println("toBytes:");
        System.out.println(toBytes);

        RecordBloEntry recordEntry = RecordBloEntry.fromBytes(toBytes);
        System.out.println("recordEntry:" + recordEntry);

        Assert.assertTrue(StringUtils.equals(recordEntry.getProcessId(), entry.getProcessId()));
        Assert.assertTrue(recordEntry.getSql().length() == entry.getSql().length());

        boolean eq = StringUtils.equals(recordEntry.getSql(), entry.getSql());
        Assert.assertTrue(eq);
    }
}
