package com.shopHub.utils;

/**
 * distributed lock by Redis
 */
public interface ILock {

    /**
     * try to get lock
     * @param timeoutSec lock duration,will be automatically released if it exceeds
     * @return true or false
     */
    boolean tryLock(long timeoutSec);
    void unLock();
}
