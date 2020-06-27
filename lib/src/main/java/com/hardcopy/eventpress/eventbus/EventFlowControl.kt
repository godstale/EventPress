package com.hardcopy.eventpress.eventbus

import hu.akarnokd.rxjava2.operators.FlowableTransformers
import io.reactivex.FlowableTransformer
import io.reactivex.processors.PublishProcessor

/**
 * Control the backpressure and valve settings.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-23.
 */
class EventFlowControl(private val backpresssure: BpType,
                       private val useValve: Boolean) {

    enum class BpType(val typeName: String) {
        BUFFER("Backpressure::BUFFER"),
        DROP("Backpressure::DROP"),
        LATEST("Backpressure::LATEST")
    }

    private val valveProcessor = PublishProcessor.create<Boolean>()
    private var isTransformerComposed = false

    /**
     * Returns FlowableTransformer for the backpressure control.
     *
     * @return  FlowableTransformer<T, T>
     */
    fun <T> getBpTransformer(): FlowableTransformer<T, T> {
        isTransformerComposed = true
        return when(backpresssure) {
            BpType.BUFFER -> FlowableTransformer { upstream ->
                upstream.onBackpressureBuffer()
            }
            BpType.DROP -> FlowableTransformer { upstream ->
                upstream.onBackpressureDrop()
            }
            BpType.LATEST -> FlowableTransformer { upstream ->
                upstream.onBackpressureLatest()
            }
        }
    }

    /**
     * Returns FlowableTransformer for the valve control.
     *
     * @return  FlowableTransformer<T, T>
     */
    fun <T> getValveTransformer(): FlowableTransformer<T, T> {
        isTransformerComposed = true
        return if(useValve) {
            FlowableTransformers.valve(valveProcessor, true)
        } else {
            FlowableTransformer { upstream -> upstream }
        }
    }

    /**
     * Returns backpressure enum type name.
     *
     * @return  String
     */
    fun getBpTypeName(): String {
        return backpresssure.typeName
    }

    /**
     * Close or open the valve. Only works when useValve is true.
     *
     * @return  Boolean
     */
    fun switchValve(open: Boolean): Boolean {
        return if(useValve) {
            valveProcessor.onNext(open)
            true
        } else { false }
    }

    /**
     * Check if Transformers are already used or not
     *
     * @return  Boolean
     */
    @Deprecated("Not available now.")
    fun isTransformerComposed(): Boolean {
        return isTransformerComposed
    }

    /**
     * Complete the valve processor. (When parent processor is completed)
     */
    fun finalizeValve() {
        valveProcessor.onComplete()
    }
}