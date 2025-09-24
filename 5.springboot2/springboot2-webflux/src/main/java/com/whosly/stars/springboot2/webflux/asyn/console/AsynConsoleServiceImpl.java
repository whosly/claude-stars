package com.whosly.stars.springboot2.webflux.asyn.console;


import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockBody;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.body.WALBlockResult;
import com.whosly.stars.springboot2.webflux.asyn.file.wal.rs.FutureCompact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class AsynConsoleServiceImpl implements IAsynConsoleService {
    private static final Logger logger = LoggerFactory.getLogger(AsynConsoleServiceImpl.class);

    private static final BlockingQueue<FutureCompact> activeQueue = new LinkedBlockingQueue<FutureCompact>(100);

    public AsynConsoleServiceImpl() {
        final ReaderThread t = new ReaderThread();
        t.setName("demo_Asyn_Console#1");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public CompletableFuture<WALBlockResult> submit(WALBlockBody reqData) {
        FutureCompact futureCompact = FutureCompact.builder()
                .body(reqData)
                .build();
        activeQueue.offer(futureCompact);
        logger.info("Queue offer");

        return CompletableFuture.supplyAsync(() -> {
            while(futureCompact.getFuture() == null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1L);
                    logger.info("supplyAsync sleep 1 ms");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                logger.info("submit finish");
                return futureCompact.getFuture().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    // ---------------------------------------------------------------------
    //                            ReaderThread
    // ---------------------------------------------------------------------
    private final class ReaderThread extends Thread {

        // ---------------------------------------------------------------------
        //                             Main loop
        // ---------------------------------------------------------------------

        @Override
        public void run() {
            while (true) {
                try {
                    FutureCompact futureCompact = activeQueue.take();
                    logger.info("Queue take");

                    WALBlockBody req = futureCompact.getBody();

                    if(req != null){
                        logger.info("completedFuture start");

                        CompletableFuture<WALBlockResult>  ff = CompletableFuture.completedFuture(
                                WALBlockResult.builder()
                                        .lsn(66L)
                                        .build()
                        );

                        logger.info("completedFuture finish");

                        futureCompact.setFuture(ff);
                    }
                } catch (InterruptedException e) {
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
    // ---------------------------------------------------------------------
    //                            ReaderThread
    // ---------------------------------------------------------------------
}
