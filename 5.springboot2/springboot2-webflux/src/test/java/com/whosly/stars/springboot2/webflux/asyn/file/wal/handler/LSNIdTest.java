package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import junit.framework.TestCase;
import org.junit.Assert;

public class LSNIdTest extends TestCase {

    public void testGet() {
        for (int i = 0; i < 100000; i++) {
            String lsn = LSNId.get();
            Assert.assertTrue(lsn != null);
            Assert.assertTrue(lsn.length() == 19);
        }
    }

    public void testGetN() {
        for (int i = 0; i < 100000; i++) {
            Long lsn = LSNId.getN();
            Assert.assertTrue(lsn != null);
        }
    }
}