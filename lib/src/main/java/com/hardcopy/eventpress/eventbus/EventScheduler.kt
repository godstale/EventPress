package com.hardcopy.eventpress.eventbus

import io.reactivex.FlowableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Holds scheduler(thread on observer) settings and makes scheduler transformer.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-13.
 */
class EventScheduler(private val type: Type) {
    enum class Type(val typeName: String) {
        IO("Scheduler::IO"),
        COMPUTATION("Scheduler::COMPUTATION"),
        UI("Scheduler::UI")
    }

    /**
     * Returns FlowableTransformer for the observer scheduler
     *
     * @return  FlowableTransformer<T, T>
     */
    fun <T> getTransformer(): FlowableTransformer<T, T> {
        return when(type) {
            Type.IO -> FlowableTransformer<T, T> { upstream ->
                upstream.observeOn(Schedulers.io())
            }
            Type.COMPUTATION -> FlowableTransformer<T, T> { upstream ->
                upstream.observeOn(Schedulers.computation())
            }
            Type.UI -> FlowableTransformer<T, T> {
                    upstream -> upstream.observeOn(AndroidSchedulers.mainThread())
            }
        }
    }

    /**
     * Returns scheduler type name
     *
     * @return  String
     */
    fun getSchedulerName(): String {
        return type.typeName
    }
}