package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class WALNameTest {

    @Test
    public void testGetName() {
        List<Long> tableIds = Arrays.asList(11L, 22L, 33L, 44L, 55L, 66L, 77L, 88L);

        for (Long tableId : tableIds){
            for (int i = 0; i < 10000; i++) {
                String tableName = WALName.getName(tableId);
                Assert.assertTrue(StringUtils.startsWith(tableName, WALName.PREFIX));
                Assert.assertTrue(StringUtils.endsWith(tableName, WALName.SUFFIX));
                Assert.assertTrue(tableName.length() == (21 + WALName.SUFFIX.length()));
            }
        }
    }
}