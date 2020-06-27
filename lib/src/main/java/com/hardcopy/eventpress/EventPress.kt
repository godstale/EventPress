package com.hardcopy.eventpress

import com.hardcopy.eventpress.eventbus.EventBus
import com.hardcopy.eventpress.eventbus.EventProcessor
import com.hardcopy.eventpress.eventbus.EventTopic
import com.hardcopy.eventpress.exceptions.EventPressException
import com.hardcopy.eventpress.utils.RxUtil
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * EventPress API class.
 * Every event-topic stream starts from this APIs.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-07.
 */
object EventPress {
    private lateinit var eventBus: EventBus

    /**
     * Initialize EventPress core.
     * WARNING: This call must be the first call of EventPress APIs.
     */
    fun initialize() {
        if(!::eventBus.isInitialized) {
            eventBus = EventBus()
        }
    }

    /**
     * Get an EventPressBuilder to start topic stream.
     *
     * @return  EventPressBuilder
     */
    fun builder(): EventPressBuilder {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception
        return EventPressBuilder.create(eventBus)
    }

    /**
     * Publish new event to system-default topic. (/sys/common)
     *
     * @param   contents
     * @return  Boolean
     */
    fun publish(contents: Any): Boolean {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception

        // use system topic [/sys/common], so no need to use recursive
        eventBus.publishEvent(EventTopic.TOPIC_COMMON, contents, false)
        return true
    }

    /**
     * Publish an event to specific topic.
     * "recursive = true" means deliver event to target topic and descendants.
     *
     * @param   topic           topic path
     * @param   contents
     * @param   recursive       deliver event to descendants also
     * @return  Boolean
     */
    fun publish(topic: String, contents: Any, recursive: Boolean = true): Boolean {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception
        if(!EventTopic.isValidForPublish(topic)) return false

        eventBus.publishEvent(topic, contents, recursive)
        return true
    }

    /**
     * Publish an event with class type.
     * EventPress converts class type to topic string using (/sys/class) topic path.
     * ex> com.exam.event.MainActivity -> /sys/class/com.exam.event.MainActivity
     *
     * @param   clazz           class type
     * @param   contents
     * @param   recursive       deliver event to descendants also
     * @return  Boolean
     */
    fun publish(clazz: Class<*>, contents: Any, recursive: Boolean = true): Boolean {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception

        eventBus.publishEvent(EventTopic.getClassTopicString(clazz), contents, recursive)
        return true
    }

    /**
     * Remove topic and descendants. (Always removes descendants also.)
     *
     * @param   topic
     */
    fun remove(topic: String) {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception
        if(!EventTopic.isValidForRemove(topic)) return

        eventBus.removeEventProcessors(topic)
    }

    /**
     * Observes default(/sys/common) topic with lamda.
     *
     * @param   onNext          lamda function to receive event
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(onNext: (T) -> Unit): Disposable? {
        return observe(EventTopic.TOPIC_COMMON, onNext, {}, {}, null)
    }

    /**
     * Observes default(/sys/common) topic with onNext, onError lamda function.
     *
     * @param   onNext      lamda function to receive event
     * @param   onError     lamda function to receive error event
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(onNext: (T) -> Unit, onError: (t: Throwable) -> Unit): Disposable? {
        return observe(EventTopic.TOPIC_COMMON, onNext, onError, {}, null)
    }

    /**
     * Observes class(/sys/class/xxx) topic with class type and lamda function.
     *
     * @param   onNext      lamda function to receive event
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(clazz: Class<*>, onNext: (T) -> Unit): Disposable? {
        return observe(EventTopic.getClassTopicString(clazz), onNext, {}, {}, null)
    }

    /**
     * Observes class(/sys/class/xxx) topic with class type and onNext, onError lamda function.
     *
     * @param   clazz       class type
     * @param   onNext      lamda function to receive event
     * @param   onError     lamda function to receive error event
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(clazz: Class<*>, onNext: (T) -> Unit, onError: (t: Throwable) -> Unit): Disposable? {
        return observe(EventTopic.getClassTopicString(clazz), onNext, onError, {}, null)
    }

    /**
     * Observes topic and onNext lamda function.
     *
     * @param   topic       topic path
     * @param   onNext      lamda function to receive event
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(topic: String, onNext: (T) -> Unit): Disposable? {
        return observe(topic, onNext, {}, {}, null)
    }

    /**
     * Observes topic and onNext, onError lamda function.
     *
     * @param   topic       topic path
     * @param   onNext      lamda function to receive event
     * @param   onError     lamda function to receive error event
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(topic: String,
                    onNext: (T) -> Unit, onError: (t: Throwable) -> Unit): Disposable? {
        return observe(topic, onNext, onError, {}, null)
    }

    /**
     * Observes topic with lamda/composite disposable.
     *
     * @param   topic       topic path
     * @param   onNext      lamda function to receive event
     * @param   onError     lamda function to receive error event
     * @param   onComplete  lamda function to receive complete event
     * @param   compositeDisposable     add disposable to this after subscribe()
     * @return  Disposable?     WARNING: release resource with disposable after use
     */
    fun <T> observe(topic: String,
                    onNext: (T) -> Unit,
                    onError: (t: Throwable) -> Unit,
                    onComplete: () -> Unit,
                    compositeDisposable: CompositeDisposable? = null): Disposable? {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception
        if(!EventTopic.isValidForSubscribe(topic)) return null

        val eventProcessor = getTopicOrCreate(topic)

        var disposable: Disposable? = null
        eventProcessor?.also {
            try {
                disposable = it.getFlowable<T>()
                    .subscribe({ event ->
                        onNext(event)
                    }, { throwable -> onError(throwable)
                    }, { onComplete() })
            } catch (e: Exception) {
                return RxUtil.makeExceptionFlowable<T>(EventPressException.TYPE_CASTING_FAILED.exception)
                    .subscribe({ event ->
                        onNext(event)
                    }, { throwable -> onError(throwable)
                    }, { onComplete() })
            }
            if(disposable != null) {
                if(compositeDisposable != null) {
                    compositeDisposable.add(disposable!!)
                } else {
                    it.addDisposable(disposable!!)
                }
            }
        }
        if(disposable == null) {
            return RxUtil.makeExceptionFlowable<T>(EventPressException.OBSERVE_FAILED.exception)
                .subscribe({ event ->
                    onNext(event)
                }, { throwable -> onError(throwable)
                }, { onComplete() })
        }

        return disposable
    }

    /**
     * Get flowable with class type to access topic stream directly.
     *
     * @param   clazz       class type
     * @return  Flowable<T> WARNING: Changes on flowable would affect other subscribers.
     */
    fun <T> getTopicFlowable(clazz: Class<*>): Flowable<T> {
        return getTopicFlowable<T>(EventTopic.getClassTopicString(clazz))
    }

    /**
     * Get flowable with topic string to access topic stream directly.
     *
     * @param   topic       topic path
     * @return  Flowable<T> WARNING: Changes on flowable would affect other subscribers.
     */
    fun <T> getTopicFlowable(topic: String): Flowable<T> {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception
        if(!EventTopic.isValidForSubscribe(topic))
            return RxUtil.makeExceptionFlowable(EventPressException.OBSERVE_FAILED.exception)

        val eventProcessor = eventBus.getProcessor(topic)
            ?: return RxUtil.makeExceptionFlowable(EventPressException.OBSERVE_FAILED.exception)

        return try {
            eventProcessor.getFlowable<T>()
        } catch (e: Exception) {
            RxUtil.makeExceptionFlowable(EventPressException.TYPE_CASTING_FAILED.exception)
        }
    }

    /**
     * Close or open the valve on topic stream.
     *
     * @param   clazz       class type
     * @param   open        open or close
     * @return  Boolean     success/fail
     */
    fun switchTopicValve(clazz: Class<*>, open: Boolean): Boolean {
        return switchTopicValve(EventTopic.getClassTopicString(clazz), open)
    }

    /**
     * Close or open the valve on topic stream.
     *
     * @param   topic       topic string
     * @param   open        open or close
     * @return  Boolean     success/fail
     */
    fun switchTopicValve(topic: String, open: Boolean): Boolean {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception
        if(!EventTopic.isValidForSubscribe(topic)) return false

        val eventProcessor = eventBus.getProcessor(topic) ?: return false
        return eventProcessor.switchProcessor(open)
    }

    /**
     * Removes disposable from EventProcessor's disposable list
     *
     * @param   topic       topic string
     * @param   disposable  disposable instance
     * @return  Boolean     success/fail
     */
    fun removeDisposable(topic: String, disposable: Disposable): Boolean {
        if(!::eventBus.isInitialized) throw EventPressException.NOT_INITIALIZED.exception

        val eventProcessor = eventBus.getProcessor(topic)?: return false

        return eventProcessor.removeDisposable(disposable)
    }

    /**
     * Create topic if not exist and get EventProcessor instance to control topic.
     *
     * @param   topic       topic string
     * @return  EventProcessor      EventProcessor instance to control topic
     */
    private fun getTopicOrCreate(topic: String): EventProcessor? {
        createTopicIfNotExist(topic)
        return eventBus.getProcessor(topic)
    }

    /**
     * Create topic if not exist.
     *
     * @param   topic       topic string
     * @return  EventProcessor      EventProcessor instance to control topic
     */
    private fun createTopicIfNotExist(topic: String) {
        eventBus.getProcessor(topic)?: run {
            builder().setTopic(topic).build()
        }
    }
}