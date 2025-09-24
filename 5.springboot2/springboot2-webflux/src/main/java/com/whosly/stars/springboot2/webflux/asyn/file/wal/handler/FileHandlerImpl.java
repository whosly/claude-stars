package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks.WALTrailer;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.io.IoManager;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.rs.FutureCompact;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.IFileHandler;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.queue.IQueueManager;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.blocks.WALHeader;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.io.writer.FileWriter;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.io.writer.IFileWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class FileHandlerImpl implements IFileHandler, IFileHandlerOpera {
    private static final Logger logger = LoggerFactory.getLogger(FileHandlerImpl.class);

    /**
     * 文件最大 64KB = 65536 byte
     */
    private static final Long K64_SIEZE_BYTE = 64L * 1024;

    /**
     * 文件最大 521KB
     */
    private static final Long K512_SIEZE_BYTE = 8 * K64_SIEZE_BYTE;

    /**
     * 文件最大 1MB
     */
    private static final Long M1_SIEZE_BYTE = 1L * 2 * K512_SIEZE_BYTE;

    /**
     * 文件最大 4MB
     */
    private static final Long M4_SIEZE_BYTE = 4L * M1_SIEZE_BYTE;

    /**
     * 文件最大 4MB
     */
    private static final Long MAX_SIEZE_BYTE = M1_SIEZE_BYTE;

    /**
     * 当前实例锁
     */
    private final Object lockObj;

    @Getter
    private final Long databaseId;
    @Getter
    private final Long tableId;

    private final IQueueManager<FutureCompact> queueManager;

    private IoManager ioManager;
    private CurrentManager currentManager;

    private IFileWriter writer;
    private File file;
    @Getter
    private String fileName;

    /**
     * 当前最新的未分配的 lsn的值
     */
    @Setter
    private Long nextLSN;

    /**
     * 当前已经分配的 lsn的值
     */
    @Setter
    private Long lasterLSN;

    /**
     * 句柄是否关闭
     */
    private volatile boolean isClosed = true;

    public FileHandlerImpl(FileSchmaData fileSchmaData, IQueueManager<FutureCompact> queueManager, CurrentManager currentManager, IoManager ioManager)
            throws IOException {
        this.lockObj = new Object();
        this.databaseId = fileSchmaData.getDatabaseId();
        this.tableId = fileSchmaData.getTableId();
        this.queueManager = queueManager;
        this.currentManager = currentManager;
        this.ioManager = ioManager;

        // long  19 length
        Long ll = 1234567890123456789L;

        // current 文件不存在，所以 prev 文件给21byte的空值
        String prevFileName = currentManager.isCurrentFileExist() ?
                fileSchmaData.getSourceFileName() : StringUtils.leftPad("", 21, "-");

        // 会尝试生成文件, 文件名不会变化
        WALCompatibilityAction compatibilityAction = WALCompatibilityAction.of(fileSchmaData);
        logger.trace("databaseId {} tableId {} 取 wal文件 {}, open时文件状态:{}",
                databaseId, tableId, compatibilityAction.getFileName(), compatibilityAction);

        this.fileName = compatibilityAction.getFileName();
        this.file = new File(WALCompatibilityAction.getFilePath(compatibilityAction));
        this.writer = new FileWriter(file);

        // write header
        if(compatibilityAction.getModel().isOpenAndCreate()
                || compatibilityAction.getModel().getOpenSize() == 0) {
            setNextLSN(LSNId.getN());

            WALHeader walHeader = new WALHeader((byte)1, getNextLSN(), prevFileName);
            byte[] walHeaderBytes = walHeader.toBytes();
            appendFile(walHeaderBytes);
        }

        if(compatibilityAction.getModel().isOpenAndCreate()){
            // 重新写入 current 文件
            currentManager.writeContext(compatibilityAction.getFileName());

            compatibilityAction.getModel().setOpenAndCreate(false);
        }

        this.isClosed = false;
    }

    private Long getLasterLSN() {
        return lasterLSN;
    }

    private Long getNextLSN() {
        return nextLSN;
    }

    public Long getLSN() {
        if(getNextLSN() != null){
            synchronized (this){
                if(getNextLSN() != null){
                    setLasterLSN(getNextLSN());
                    setNextLSN(null);

                    return getLasterLSN();
                }
            }
        }

        // nextLSN == null
        setLasterLSN(LSNId.getN());
        return getLasterLSN();
    }

    @Override
    public CompletableFuture<WALBlockResult> commit(WALBlockBody walBlockBody) {
        FutureCompact futureCompact = FutureCompact.builder()
                .fileHandler(this)
                .body(walBlockBody)
                .lsnSeq(getLSN())
                .build();
        queueManager.offer(futureCompact);
        logger.debug("Queue offer, lsn:{}.", futureCompact.getLsnSeq());

        return CompletableFuture.supplyAsync(() -> {
            while(futureCompact.getFuture() == null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                logger.debug("submit finish, waiting");
                return futureCompact.getFuture().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public Long appendFile(byte[] val) throws IOException {
        retryMoveNewFile(val.length);

        // 二进制协议， 不补换行
        IoManager.IOTimeBean timeBean = ioManager.startIOTime();
        writer.write(val);
        timeBean.end();

        return -1L;
    }

    /**
     * 生成新文件
     */
    private boolean retryMoveNewFile(int addLen) throws IOException {
        // 文件可能被误删除。 因此需要判断 file.exists()
        // 判断当前文件存在与否和大小
        long len = file.length();
        if(file.exists() & (len + addLen) < MAX_SIEZE_BYTE - 50){
            // do nothing
            return false;
        }

        synchronized (lockObj){
            if(file.exists() & (file.length() + addLen) <= MAX_SIEZE_BYTE - 50){
                // do nothing
                return false;
            }

            String currentFileName = this.fileName;
            // 产生新的文件名
            String newFileName = WALName.getName(tableId);

            if(file.exists()){
                // 当前文件追加结束标记
                endMark(newFileName);
            }

            // 会尝试生成新文件
            WALCompatibilityAction compatibilityAction = WALCompatibilityAction.of(
              FileSchmaData.builder()
                      .databaseId(databaseId).tableId(tableId).sourceFileName(newFileName)
                      .build()
            );

            // 写入头部数据，绑定在当前句柄下
            this.fileName = compatibilityAction.getFileName();
            this.file = new File(WALCompatibilityAction.getFilePath(compatibilityAction));
            this.writer = new FileWriter(file);

            if(compatibilityAction.getModel().isOpenAndCreate()){
                // 重新写入 current 文件
                currentManager.writeContext(compatibilityAction.getFileName());
            }
            // write header
            if(compatibilityAction.getModel().isOpenAndCreate()
                    || compatibilityAction.getModel().getOpenSize() == 0) {

                setNextLSN(LSNId.getN());

                WALHeader walHeader = new WALHeader((byte)1, getNextLSN(), currentFileName);
                byte[] walHeaderBytes = walHeader.toBytes();
                appendFile(walHeaderBytes);

                compatibilityAction.getModel().setOpenAndCreate(false);
            }
        }

        return true;
    }

    @Override
    public Long getIOCostTime(){
        return ioManager.getIOCostTime();
    }

    @Override
    public boolean check() {
        if(this.isClosed){
            return false;
        }

        if(!this.file.exists()){
            // 句柄持有的文件不存在
            return false;
        }

        if(!currentManager.isCurrentFileExist()){
            return false;
        }

        return true;
    }

    @Override
    public boolean close() {
        if(this.isClosed){
            return false;
        }

        boolean rs = true;
        synchronized (lockObj){
            if(this.isClosed){
                return false;
            }

            try {
                // 文件不追加结束标记

                // 关闭文件流
                writer.close();

                this.isClosed = true;
            } catch (IOException e) {
                e.printStackTrace();

                rs = false;
            }

            currentManager.close();
            currentManager = null;

            ioManager.close();
            ioManager = null;

        }

        return rs;
    }

    private boolean endMark(String nextFileName) throws IOException {
        // 文件追加结束标记
        byte[] endByte = new WALTrailer(nextFileName).toBytes();

        IoManager.IOTimeBean timeBean = ioManager.startIOTime();
        this.writer.write(endByte);
        timeBean.end();

        return true;
    }

}
