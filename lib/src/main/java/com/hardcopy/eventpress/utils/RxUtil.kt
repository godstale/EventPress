package com.hardcopy.eventpress.utils

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/**
 * ReactiveX toolkit.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-15.
 */
object RxUtil {
    /**
     * Make simple flowable instance to avoid "return null" to subscriber.
     *
     * @param   item
     * @return  Flowable<T>
     */
    fun <T> makeDummyFlowable(item: T): Flowable<T> {
        return Flowable.just(item)
    }

    /**
     * Make flowable which publish onError() right after .subscribe().
     * Use this to notify error to subscriber instead "return null".
     *
     * @param   item
     * @return  Flowable<T>
     */
    fun <T> makeExceptionFlowable(exception: RuntimeException): Flowable<T> {
        return Flowable.create({
                    it.onError(exception)
                }, BackpressureStrategy.MISSING)
    }
}