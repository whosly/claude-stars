package com.yueny.study.netty.allocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 堆外分配器的管理器，包含对自身和 ByteBufPooledAllocator 的管理
 *
 * @author fengyang
 * @date 2023/8/14 下午4:14
 * @see <a href="yueny09@163.com">mailTo: yueny09@163.com</a>
 */
public class ByteBufPoolManager
{
    private static final Logger log = LoggerFactory.getLogger(ByteBufPoolManager.class);

    private static final ByteBufPoolManager INSTANCE = new ByteBufPoolManager();

    public static ByteBufPoolManager getInstance() {
        return INSTANCE;
    }

    /**
     * 堆外内存分配管理器清单
     */
    private final Map<String, IDirectByteBufPooledAllocator> allocatorManagerMap;

    private ByteBufPoolManager()
    {
        this.allocatorManagerMap = new ConcurrentHashMap<>();
    }

    /**
     * 注册堆外内存分配管理器实例
     */
    public boolean register(IDirectByteBufPooledAllocator allocator) {
        if(allocator == null) {
            return false;
        }

        if(this.allocatorManagerMap.containsKey(allocator.getName())) {
            return false;
        }

        this.allocatorManagerMap.putIfAbsent(allocator.getName(), allocator);
        return true;
    }

    /**
     * 创建 ByteBufPooledAllocator
     */
    public DirectByteBufPooledAllocator createDirectByteBufPooled() {
        DirectByteBufPooledAllocator pool = DirectByteBufPooledAllocator.getInstance();

        return pool;
    }

    /**
     * 当前分配器缓存分配的所有的堆外内存总大小。单位 byte
     *
     * @return 实际分配的总的字节大小。如果不存在分配器，则返回 -1
     */
    public long getUsedDirectMemory(String allocatorName) {
        Optional<IDirectByteBufPooledAllocator> pooledAllocator = getAllocatorManager(allocatorName);

        if(pooledAllocator.isPresent()) {
            return pooledAllocator.get().getUsedDirectMemory();
        }

        return -1L;
    }

    /**
     * 是否存在指定 allocatorName 的内存分配器
     *
     * @return  true 存在
     */
    private boolean isExistAllocatorManager(String allocatorName) {
        return this.allocatorManagerMap.containsKey(allocatorName);
    }

    /**
     * 获取指定 allocatorName 的内存分配器
     */
    private Optional<IDirectByteBufPooledAllocator> getAllocatorManager(String allocatorName) {
        if(!isExistAllocatorManager(allocatorName)) {
            return Optional.empty();
        }

        return Optional.of(this.allocatorManagerMap.get(allocatorName));
    }
}
