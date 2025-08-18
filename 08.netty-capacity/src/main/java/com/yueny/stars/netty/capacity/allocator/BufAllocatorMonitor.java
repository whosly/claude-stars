package com.yueny.stars.netty.capacity.allocator;

import com.yueny.stars.netty.capacity.ThreadPools;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author fengyang
 * @date 2023/8/14 下午4:56
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
@Slf4j
public class BufAllocatorMonitor
{
    private final IDirectByteBufPooledAllocator allocatorManager;
    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * 当前分配器分配的所有的堆外内存总大小。单位 byte
     */
    private AtomicLong totalAllocatorMem;

    public BufAllocatorMonitor(IDirectByteBufPooledAllocator allocatorManager)
    {
        this.allocatorManager = allocatorManager;
        this.totalAllocatorMem = new AtomicLong(0L);

        // start monitor
        this.scheduledExecutorService = ThreadPools.createSingleScheduledExecutor("connBufAllocM", log);

        this.scheduledExecutorService.scheduleWithFixedDelay(new ConnectionBufAllocatorMontorRunner(this, this.allocatorManager),
                0,
                10,
                TimeUnit.SECONDS
        );
    }

    public AtomicLong getTotalAllocatorMem()
    {
        return totalAllocatorMem;
    }

    private void setTotalAllocatorMem(long totalAllocatorMem)
    {
        this.totalAllocatorMem.set(totalAllocatorMem);
    }

    class ConnectionBufAllocatorMontorRunner
            implements Runnable {
        private static Logger logger = LoggerFactory.getLogger(ConnectionBufAllocatorMontorRunner.class);

        private final IDirectByteBufPooledAllocator allocatorManager;
        private final BufAllocatorMonitor monitor;

        private ConnectionBufAllocatorMontorRunner(BufAllocatorMonitor monitor, IDirectByteBufPooledAllocator allocatorManager) {
            this.monitor = monitor;
            this.allocatorManager = allocatorManager;
        }

        @Override
        public void run()
        {
            log.info("ConnectionBufAllocatorMontorRunner start.");

            if(this.allocatorManager == null) {
                log.warn("allocatorManager is null. ConnectionBufAllocatorMontorRunner finish.");
                return;
            }

//            try {
//                // loop 每一个conn 的 buffer。 如果失败，则忽略
//                Map<Long, ByteBufStaticInfo> staticInfoMap = this.allocatorManager.getByteBufStaticInfo();
//
//                // 当前分配器分配的所有的堆外内存总大小。单位 byte
//                long totalAllocatorMem = staticInfoMap.entrySet().stream()
//                        .collect(Collectors.summarizingInt(x->x.getValue().getCapacity())).getSum();
//                this.monitor.setTotalAllocatorMem(totalAllocatorMem);
//
//                // buffer存在检测机制，当当前使用大小小于256k时，缩容至512k. 缩容时存在互斥锁
//                Set<Long> resizeConnId = new HashSet<>();
//                for (Map.Entry<Long, ByteBufStaticInfo> staticInfoEntry : staticInfoMap.entrySet()) {
//                    try{
//                        long connId = staticInfoEntry.getKey();
//                        ByteBufStaticInfo staticInfo = staticInfoEntry.getValue();
//
//                        // 容量大于初始分配容量，且写索引小于256k
//                        if(staticInfo.getCapacity() > ByteBufPooledAllocator.getInstance().getConfigInitialCapacity() &&
//                                staticInfo.getWriterIndex() < Allocators._1K * 256) {
//                            this.allocatorManager.resize(connId);
//                            resizeConnId.add(connId);
//                        }
//                    }catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if(resizeConnId.isEmpty()) {
//                    log.info("ConnectionBufAllocatorMontorRunner finish. do nothing...");
//                }else{
//                    List<Map.Entry<Long, ByteBufStaticInfo>> resizeConnStatList = staticInfoMap.entrySet().stream().filter(k -> {
//                        return resizeConnId.contains(k.getKey());
//                    }).collect(Collectors.toList());
//
//                    log.info("ConnectionBufAllocatorMontorRunner finish. 当前分配器分配的所有的堆外内存总大小:{}," +
//                                    "缩容信息:{}.",
//                            BytesUtil.byteToM(totalAllocatorMem),
//                            resizeConnStatList);
//                }
//            } catch (Throwable e) {
//                log.error("ConnectionBufAllocatorMontorRunner fail, cause :", e);
//
//                e.printStackTrace();
//            }
        }
    }
}
