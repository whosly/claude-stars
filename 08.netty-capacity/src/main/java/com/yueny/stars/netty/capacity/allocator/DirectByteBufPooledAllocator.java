package com.yueny.stars.netty.capacity.allocator;

import com.google.common.collect.ImmutableMap;
import com.yueny.stars.netty.capacity.buffer.ByteBufWrappers;
import com.yueny.stars.netty.capacity.buffer.WrappedAutoFlushByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocatorMetric;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 面向连接(Connection) 的 buffer 分配
 *
 * 涉及到对内存的缓存、分配和释放策略。
 *
 * 是对内存做了池化，也就是缓存一定容量的内存，每次申请ByteBuf时，不需要重新向操作系统或者JVM申请内存，
 * 而是可以直接从预先申请好的内存池中取一块内存（类似于线程池）。
 *
 * 因此在需要频繁申请和释放内存的场景下，性能明显更好。
 *
 * @author fengyang
 * @date 2023/8/10 上午10:49
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class DirectByteBufPooledAllocator
        implements IDirectByteBufPooledAllocator
{
    private Logger log = LoggerFactory.getLogger(DirectByteBufPooledAllocator.class);

    ////////////////////////////////////////////////////////
    //                      protocol                      //
    ////////////////////////////////////////////////////////
    /**
     * MYSQL 协议层允许占用的最大内存。 默认2G
     */
    public static final long DEFAULT_MAX_ALLOCATOR_MEM = 3 * Allocators._512MB;

    ////////////////////////////////////////////////////////
    //                  AllocatorManager                  //
    ////////////////////////////////////////////////////////
    /**
     * heap arena默认数量. [设置不会影响 usedDirectMemory]
     *
     * 默认 min(cpu核数，maxMemory/chunkSize/6)，一般来说会=cpu核数
     */
    private static final int DEFAULT_ALLOC_NUM_HEAP_ARENA = 0;

    /**
     * direct arena默认数量 [设置不会影响 usedDirectMemory]
     *
     * 默认 min(cpu核数，directMemory/chunkSize/6)，一般来说会=cpu核数
     */
    private static final int DEFAULT_ALLOC_NUM_DIRECT_ARENA = 6;

    /**
     * 页默认大小为 8192b, 8KiB. [设置不会影响 usedDirectMemory]
     */
    private static final int DEFAULT_ALLOC_PAGE_SIZE = 4 * Allocators._1K;

    /**
     *  [设置不会影响 usedDirectMemory]
     *
     * default_max_order用来管理chunk的大小，因为chunk是以平衡二叉树的形式管理所有的page，树的深度决定chunk的大小。
     *
     * defualt_max_order 默认为11，则chunk的大小约为 DEFAULT_PAGE_SIZE*(2^11)
     *
     *  一个chunk的大小 实际值为：pageSize << maxOrder.
     * DEFAULT_PAGE_SIZE << 11 = 8 MiB per chunk
     *
     * 相关计算：
     * 4 * _1K << 5 :128KB
     * 4 * _1K << 6 :256KB
     * 4 * _1K << 7 :512KB
     * 4 * _1K << 8 :1024KB
     * 4 * _1K << 9 :2MB
     * 4 * _1K << 10 :4MB
     * 4 * _1K << 11 :8MB
     * 4 * _1K << 12 :16MB
     *
     * 实际总的堆外内存的最大使用值为：
     *      DEFAULT_ALLOC_NUM_DIRECT_ARENA * chunk的大小 -->
     *      DEFAULT_ALLOC_NUM_DIRECT_ARENA * (DEFAULT_ALLOC_PAGE_SIZE << DEFAULT_ALLOC_MAX_ORDER)
     */
    private static final int DEFAULT_ALLOC_MAX_ORDER = 7;

    ////////////////////////////////////////////////////////
    //                Connection Property                 //
    ////////////////////////////////////////////////////////
    /**
     * [设置不会影响 usedDirectMemory]
     *
     * 申请buffer时， 默认的初始化容量。默认 4096， 即 4KiB
     */
    private static final int DEFAULT_CONN_INITIAL_CAPACITY = 4 * Allocators._1K;

    /**
     *  [设置会影响 usedDirectMemory] 会！！！
     *
     * 申请buffer时， 默认的最大的容量。默认 4096 = 4 KiB.
     *
     * 4096 = 4 KiB
     * 5242880 = 5MB
     */
    private static final long DEFAULT_CONN_MAX_CAPACITY = 4 * Allocators._1MB;

    ////////////////////////////////////////////////////////
    //                       INSTANCE                     //
    ////////////////////////////////////////////////////////
    private static final DirectByteBufPooledAllocator INSTANCE = new DirectByteBufPooledAllocator();

    public static DirectByteBufPooledAllocator getInstance() {
        return INSTANCE;
    }

    //////////////////////////////////////////////////////
    //                    BufAllocatorManager           //
    //////////////////////////////////////////////////////
    private final PooledByteBufAllocator alloc;

    /**
     * destory、alloc 的互斥锁
     */
    private final Object allocLock;

    /**
     * resize 的互斥锁
     */
    private final Object actionLock;
//
//    /**
//     * 每个连接分配的 buffer 管理
//     *
//     * key is  connId, value is buf
//     */
//    private final Map<Long, ByteBuf> connBuf;
//
//    /**
//     * 每个连接分配的 buffer 属性管理
//     *
//     * key is  connId, value is buf property： ByteBufProperty
//     */
//    private final Map<Long, ByteBufProperty> connBufProp;

    private final BufAllocatorMonitor monitor;

    private DirectByteBufPooledAllocator() {
        // numThreadCaches 用于判断最少使用的PoolArena。
        //
        // Here is how the system property is used:
        //
        // * <  0  - Don't use cleaner, and inherit max direct memory from java. In this case the
        //           "practical max direct memory" would be 2 * max memory as defined by the JDK.
        // * == 0  - Use cleaner, Netty will not enforce max memory, and instead will defer to JDK.
        // * >  0  - Don't use cleaner. This will limit Netty's total direct memory
        //           (note: that JDK's direct memory limit is independent of this).

        this.alloc = new PooledByteBufAllocator(
                true,
                DEFAULT_ALLOC_NUM_HEAP_ARENA,
                DEFAULT_ALLOC_NUM_DIRECT_ARENA,
                DEFAULT_ALLOC_PAGE_SIZE,
                DEFAULT_ALLOC_MAX_ORDER
        );

        this.allocLock = new Object();
        this.actionLock = new Object();

        this.monitor = new BufAllocatorMonitor(this);

        ByteBufPoolManager.getInstance().register(this);
    }

    /**
     * MYSQL 协议层允许占用的最大内存。 默认2G
     */
    public long getConfigMaxAllocatorMem() {
        return DEFAULT_MAX_ALLOCATOR_MEM;
    }

    public int getConfigInitialCapacity() {
        return DEFAULT_CONN_INITIAL_CAPACITY;
    }

    public Long getConfigMaxCapacity() {
        return DEFAULT_CONN_MAX_CAPACITY;
    }

    @Override
    public String alias()
    {
        return "direct-bytebuf-alloc";
    }

    @Override
    public String getName()
    {
        return DirectByteBufPooledAllocator.class.getSimpleName();
    }

    @Override
    public int cacheSize()
    {
        return this.alloc.metric().normalCacheSize() +
                this.alloc.metric().smallCacheSize();
    }

    /**
     * 分配器的统计信息
     */
    @Override
    public ByteBufAllocatorMetric allocatorMetric()
    {
        PooledByteBufAllocatorMetric metric = alloc.metric();

        return metric;
    }

    @Override
    public long getUsedDirectMemory()
    {
        return this.allocatorMetric().usedDirectMemory();
    }

    @Override
    public WrappedAutoFlushByteBuf allocByteBuf()
    {
        return allocByteBuf(getConfigInitialCapacity());
    }

    @Override
    public WrappedAutoFlushByteBuf allocByteBuf(int initialCapacity)
    {
        return allocByteBuf(initialCapacity, getConfigMaxCapacity());
    }

    @Override
    public WrappedAutoFlushByteBuf allocByteBuf(Long maxCapacity)
    {
        return allocByteBuf(getConfigInitialCapacity(), maxCapacity);
    }

    @Override
    public WrappedAutoFlushByteBuf allocByteBuf(int initialCapacity, Long maxCapacity)
    {
        return allocByteBuf(initialCapacity, maxCapacity, true);
    }

    @Override
    public WrappedAutoFlushByteBuf allocByteBuf(int initialCapacity, Long maxCapacity, boolean scaleDown)
    {
        ByteBuf byteBuf;
        synchronized (allocLock) {
            // check size
            //.

            byteBuf = alloc.directBuffer(initialCapacity, maxCapacity.intValue());
        }

        return ByteBufWrappers.wrapper(byteBuf, null);
    }

    @Override
    public void release(ByteBuf byteBuf) {
        synchronized (allocLock) {
            byteBuf.release();
        }
    }

    @Override
    public void resize(ByteBuf byteBuf)
    {
        synchronized (actionLock) {
            if(byteBuf.capacity() > getConfigInitialCapacity()) {
                log.warn("Inconsistency between actual capacity and expected capacity after downsizing. " +
                                "actual capacity: {}, expected capacity: {}.",
                        byteBuf.capacity(), getConfigInitialCapacity());

                // add conn warnings
            }
        }
    }

    @Override
    public Map<Long, ByteBufStaticInfo> getByteBufStaticInfo() {
        ImmutableMap.Builder<Long, ByteBufStaticInfo> staticInfoBuilder = ImmutableMap.builder();

        return staticInfoBuilder.build();
    }

}
