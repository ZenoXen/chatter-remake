package org.zh.chatter.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {

    private final Map<String, ReentrantLock> lockMap;

    public LockManager() {
        this.lockMap = new ConcurrentHashMap<>();
    }

    private ReentrantLock addLock(String lockKey) {
        ReentrantLock lock = new ReentrantLock(true);
        lockMap.put(lockKey, lock);
        return lock;
    }

    public void runWithLock(String lockKey, Runnable runnable) {
        ReentrantLock lock = lockMap.get(lockKey);
        if (lock == null) {
            lock = this.addLock(lockKey);
        }
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }
}
