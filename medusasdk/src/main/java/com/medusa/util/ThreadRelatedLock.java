package com.medusa.util;

import java.util.HashMap;

public class ThreadRelatedLock {

    private final long mThreadId;

    private ThreadRelatedLock(long threadId) {
        mThreadId = threadId;
    }

    public long getThreadId() {
        return mThreadId;
    }

    @Override
    public String toString() {
        return "ThreadRelatedLock{" +
                "mThreadId=" + mThreadId +
                '}';
    }

    private static HashMap<Long, ThreadRelatedLock> sClassLoaderLocks = new HashMap<Long, ThreadRelatedLock>();

    public static ThreadRelatedLock createClassLoaderLock(long threadId) {
        ThreadRelatedLock lock = sClassLoaderLocks.get(threadId);
        if (null == lock) {
            lock = new ThreadRelatedLock(threadId);
            sClassLoaderLocks.put(threadId, lock);
        }
        return lock;
    }
}
