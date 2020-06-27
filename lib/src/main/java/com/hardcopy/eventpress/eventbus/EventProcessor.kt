package com.hardcopy.eventpress.eventbus

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor

/**
 * EventProcess holds every settings and instances related to the topic.
 * EventPressBuilder make and initializes this instance and
 * EventBus makes main worker, PublishProcessor, at registration time.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-15.
 */
class EventProcessor(val topic: EventTopic,
                     private val scheduler: EventScheduler,
                     private val flowControl: EventFlowControl) {

    private var processor: FlowableProcessor<Any>? = null
    private var compositeDisposable = CompositeDisposable()

    /**
     * Set PublishProcessor instance.
     * FlowableProcessor is interface name of PublishProcessor
     *
     * @param   FlowableProcessor<Any>
     */
    fun setProcessor(fp: FlowableProcessor<Any>) {
        processor = fp
    }

    /**
     * Return PublishProcessor instance.
     *
     * @return   FlowableProcessor<Any>
     */
    fun getProcessor(): FlowableProcessor<Any>? {
        return processor
    }

    /**
     * Return PublishProcessor instance as Flowable.
     * Flowable supports .subscribe() and various operator on it.
     *
     * @return   FlowableProcessor<T>
     */
    fun <T> getFlowable(): Flowable<T> {
        var flowableProcessor = processor as FlowableProcessor<T>
        return flowableProcessor
            .compose(flowControl.getBpTransformer())
            .compose(flowControl.getValveTransformer())
            .compose(scheduler.getTransformer<T>()) as Flowable
    }

    /**
     * Close or open the valve if valve is available
     *
     * @param   open
     */
    fun switchProcessor(open: Boolean): Boolean {
        return flowControl.switchValve(open)
    }

    /**
     * Stop PublishProcessor stream and release resources.
     */
    fun stopProcessor() {
        disposeAll()
        flowControl.finalizeValve()
        processor?.run {
            if(!hasComplete()) {
                onComplete()
            }
        }
    }

    /**
     * Dispose all the disposals on this processor
     */
    fun disposeAll() {
        compositeDisposable?.dispose()
    }

    /**
     * Returns the count of how many observers are on this processor.
     * But this count is not reliable.
     *
     * @return  Int     count of observer
     */
    fun getDisposableCount(): Int {
        return compositeDisposable.size()
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable?.add(disposable)
    }

    fun removeDisposable(disposable: Disposable): Boolean {
        if(!disposable.isDisposed) {
            disposable.dispose()
        }
        return compositeDisposable.remove(disposable)
    }
}