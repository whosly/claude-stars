package com.whosly.stars.springboot2.webflux.asyn.file.wal.io;

import cn.hutool.core.date.SystemClock;

import java.util.concurrent.atomic.AtomicLong;

public class IoManager {
    private AtomicLong ioTimer;

    private Long tableId;

    public IoManager(Long tableId) {
        this.tableId = tableId;
        this.ioTimer = new AtomicLong(0);
    }

    public IOTimeBean startIOTime(){
        return new IOTimeBean(this);
    }

    /**
     * 当前耗时
     */
    public Long getIOCostTime(){
        return ioTimer.get();
    }

    /**
     * close
     */
    public void close(){
        tableId = null;
        ioTimer = null;
    }

    private void addIO(long times){
        ioTimer.addAndGet(times);
    }

    private static class TimeBean {
        private final Long start;

        protected TimeBean() {
            this.start = SystemClock.now();
        }

        protected long finish(){
            long times = SystemClock.now() - start;

            return times;
        }
    }

    public static final class IOTimeBean extends TimeBean {

        private final IoManager ioManager;

        public IOTimeBean(IoManager ioManager) {
            super();
            this.ioManager = ioManager;
        }

        public void end(){
            long times = finish();

            ioManager.addIO(times);
        }
    }

}
