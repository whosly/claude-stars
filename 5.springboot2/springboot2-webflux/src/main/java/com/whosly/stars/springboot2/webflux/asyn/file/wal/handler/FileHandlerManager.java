package com.whosly.stars.springboot2.webflux.asyn.file.wal.handler;

import cn.hutool.core.io.FileUtil;
import com.google.common.io.Files;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.io.IoManager;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.rs.FutureCompact;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.IFileHandler;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.queue.IQueueManager;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.queue.QueueManagerImpl;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.block.WALBlockLog;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordBloEntry;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.fs.entry.RecordHeaderBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FileHandlerManager {
    private static final Logger logger = LoggerFactory.getLogger(FileHandlerManager.class);

    /**
     * 当前实例锁
     */
    private static final Object LOCK_OBJ = new Object();

    private static final Map<Long, IFileHandlerOpera> HANDLER_MAP = new ConcurrentHashMap<>();

    private static final IQueueManager<FutureCompact> queueManager;

    static {
        queueManager = new QueueManagerImpl();

        ReaderThread t = new ReaderThread();
        t.setName("wal-M#1");
        t.setDaemon(true);
        t.start();
    }

    public static final Optional<IFileHandler> tryGet(Long databaseId, Long tableId) {
        // 命中 cache
        if(containsKey(tableId)){
            logger.trace("databaseId {} tableId {} 命中 handler 缓存",
                    databaseId, tableId);

            IFileHandlerOpera fileHandler = (IFileHandlerOpera) get(tableId);
            if(fileHandler.check()){
                // 句柄自我检查通过
                return Optional.of(fileHandler);
            }else{
                remove(tableId);
            }
        }

        // 取 CURRENT 文件
        CurrentManager currentManager = new CurrentManager(databaseId, tableId);
        IoManager ioManager = new IoManager(tableId);

        String fileName = "";
        if(currentManager.isCurrentFileExist()){
            // 读 currentFile 取 fileName
            try {
                String currentFile = currentManager.getCurrentFilePath();

                List<String> lines =  Files.readLines(new File(currentFile), Charset.forName("utf-8"));

                if(CollectionUtils.isNotEmpty(lines)){
                    fileName = lines.get(0);
                }
                logger.trace("databaseId {} tableId {} 下的 CURRENT 文件 {} 读取结果：{}",
                        databaseId, tableId, currentFile, fileName);
            } catch (IOException ex) {  // 没有 CURRENT 文件
                logger.warn("databaseId {} tableId {} 下的 CURRENT 文件读取异常:{}。",
                        databaseId, tableId, ex.getMessage());
            }
        }

        if(StringUtils.isEmpty(fileName)){
            logger.trace("databaseId {} tableId {} 下的 CURRENT 文件记录为空，重新生成文件名。",
                    databaseId, tableId, fileName);
            fileName = WALName.getName(tableId);
        }
        // check fileName
        if(!FileHandlerManager.isFileExist(databaseId, tableId, fileName)){
            logger.trace("databaseId {} tableId {} 下的 CURRENT 文件中记录的 wal文件 {} 不存在，重新生成文件名。",
                    databaseId, tableId, fileName);
            fileName = WALName.getName(tableId);
        }

        try {
            IFileHandlerOpera fileHandler = new FileHandlerImpl(FileSchmaData.builder()
                    .databaseId(databaseId)
                    .tableId(tableId)
                    .sourceFileName(fileName).build(),
                    queueManager, currentManager, ioManager);
            put(tableId, fileHandler);
            return Optional.of(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();

            // throws Exception
            return Optional.empty();
        }
    }

    public static final void close(Long tableId){
        if(containsKey(tableId)){
            synchronized (LOCK_OBJ){
                if(containsKey(tableId)){
                    IFileHandlerOpera fileHandler = (IFileHandlerOpera) get(tableId);
                    fileHandler.close();

                    HANDLER_MAP.remove(tableId);
                }
            }
        }
    }

    public static final boolean containsKey(Long tableId){
        return HANDLER_MAP.containsKey(tableId);
    }

    public static final IFileHandler get(Long tableId){
        return HANDLER_MAP.get(tableId);
    }

    private static final void put(Long tableId, IFileHandlerOpera fileHandler){
        HANDLER_MAP.put(tableId, fileHandler);
    }

    private static final IFileHandler remove(Long tableId){
        return HANDLER_MAP.remove(tableId);
    }

    /**
     * fileName 文件是否存在
     *
     * @return
     */
    public static final boolean isFileExist(Long databaseId, Long tableId, String fileName){
        boolean isExist = FileUtil.exist(WALCompatibilityAction.getFile(databaseId, tableId, fileName));

        return isExist;
    }

    // ---------------------------------------------------------------------
    //                            ReaderThread
    // ---------------------------------------------------------------------
    private static WALBlockResult compactEntry(FutureCompact futureCompact){
        IFileHandlerOpera fileHandler = futureCompact.getFileHandler();

        WALBlockBody body = futureCompact.getBody();
        Long lsnSeq = futureCompact.getLsnSeq();

        WALBlockResult rs = WALBlockResult.builder()
                .lsn(lsnSeq)
                .build();

        try {
            WALBlockLog walBlockLog = new WALBlockLog(
                    RecordHeaderBuilder.build(1, lsnSeq),
                    new RecordBloEntry(body.getProcessId(), body.getTid(), body.getSql())
            );

            byte[] fileBytes = walBlockLog.toBytes();

            fileHandler.appendFile(fileBytes);
        } catch (IOException e) {
            // TODO 写文件失败，咋搞
            e.printStackTrace();
        }

        return rs;
    }


    // ---------------------------------------------------------------------
    //                            ReaderThread
    // ---------------------------------------------------------------------
    private static final class ReaderThread extends Thread {

        // ---------------------------------------------------------------------
        //                             Main loop
        // ---------------------------------------------------------------------
        @Override
        public void run() {
            while (true) {
                try {
                    FutureCompact futureCompact = queueManager.take();
                    if(futureCompact == null || futureCompact.getBody() == null){
                        continue;
                    }

                    WALBlockResult rs = compactEntry(futureCompact);

                    CompletableFuture<WALBlockResult> ff = CompletableFuture.completedFuture(rs);
                    futureCompact.setFuture(ff);
                } catch (InterruptedException e) {
                    // TODO
                    e.printStackTrace();

                    try {
                        TimeUnit.MILLISECONDS.sleep(5L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } // end while alive
        }
    }

}
