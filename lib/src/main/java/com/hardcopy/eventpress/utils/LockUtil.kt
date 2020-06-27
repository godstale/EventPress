package com.hardcopy.eventpress.utils

import java.util.concurrent.locks.ReentrantLock

/**
 * Util class to support lock/Safety features.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-15.
 */
object LockUtil {
    /**
     * Automatically wraps code block and lock with ReentrantLock.
     *
     * @param   reLock      ReentrantLock
     * @param   body        lamda function to protect with ReentrantLock
     */
    fun <T> lock(reLock: ReentrantLock, body: () -> T): T {
        reLock.lock()
        try {
            return body()
        } finally {
            reLock.unlock()
        }
    }
}