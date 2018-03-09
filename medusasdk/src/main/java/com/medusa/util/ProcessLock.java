package com.medusa.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by scott on 17/2/17.
 */

public class ProcessLock {
    public static final String TAG = "ProcessLock";

    private File lockFile;
    private RandomAccessFile lockRaf;
    private FileChannel lockChannel;
    private FileLock cacheLock;

    public ProcessLock(String lockFilePath) {
        this.lockFile = new File(lockFilePath);
    }

    public ProcessLock(File lockFile) {
        this.lockFile = lockFile;
    }

    public void lock() {
        try {
            lockRaf = new RandomAccessFile(lockFile, "rw");
        } catch (FileNotFoundException e) {
            Log.error(TAG, "ProcessLock error", e);
            return;
        }

        if (lockRaf == null || lockFile == null) {
            Log.error(TAG, "lock error lockRaf = " + lockRaf + " lockFile = " + lockFile);
            return;
        }

        lockChannel = lockRaf.getChannel();
        Log.info(TAG, "Blocking on lock " + lockFile.getPath());
        try {
            cacheLock = lockChannel.lock();
        } catch (Throwable e) {
            Log.error(TAG, "lock error ", e);
            return;
        }
        Log.info(TAG, lockFile.getPath() + " locked");
    }

    public void unlock() {
        if (cacheLock != null) {
            try {
                cacheLock.release();
            } catch (Throwable e) {
                Log.error(TAG, "Failed to release lock on " + (lockFile != null ? lockFile.getPath() : ""));
            }
        }
        if (lockChannel != null) {
            closeQuietly(lockChannel);
        }
        closeQuietly(lockRaf);
        if (lockFile != null) {
            Log.info(TAG, lockFile.getPath() + " unlocked");
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Throwable e) {
            Log.error(TAG, "Failed to close resource", e);
        }
    }
}
