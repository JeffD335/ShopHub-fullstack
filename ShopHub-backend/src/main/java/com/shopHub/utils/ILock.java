package com.shopHub.utils;

public interface ILock {

    boolean tryLock(long timeoutSec);
    void unLock();
}
