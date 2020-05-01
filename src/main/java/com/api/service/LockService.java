package com.api.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LockService {
    private final ConcurrentHashMap<Integer, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public void takeLock(Integer accountId) throws InterruptedException {
        synchronized (lockMap) {
            if (!lockMap.containsKey(accountId)) {
                lockMap.put(accountId, new ReentrantLock());
            }
        }
        lockMap.get(accountId).tryLock(20, TimeUnit.SECONDS);

    }

    public void releaseLock(Integer accountId) {
        if (lockMap.get(accountId).isHeldByCurrentThread()) {
            lockMap.get(accountId).unlock();
        }

    }
}
