package com.whosly.stars.springboot2.webflux.asyn.file.wal.io.writer;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.util.bytes.BytesBuffers;

public class K4BytesBuffers {
    public static final int K1 = 1024;

    private Long databaseId;
    private Long tableId;
    private BytesBuffers bytesBuffer;

    public K4BytesBuffers(Long databaseId, Long tableId) {
        this.databaseId = databaseId;
        this.tableId = tableId;

        this.bytesBuffer = BytesBuffers.build(K1 * 4, false);

        // bytesBuffer.ensureCapacity(K1 * 4);
//        int sk = bytesBuffer.getLength() / K1;
//        byte[] data = bytesBuffer.array();
//
//        if(sk >= 2){
//            // 写新文件
//
//        }
    }

}
