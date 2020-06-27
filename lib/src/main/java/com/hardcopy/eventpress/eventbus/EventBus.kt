package com.hardcopy.eventpress.eventbus

import com.hardcopy.eventpress.utils.DebugUtil
import com.hardcopy.eventpress.utils.LockUtil
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Used for managing topic-processor map.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-07.
 */

class EventBus {

    private val processorMap = ConcurrentHashMap<String, EventProcessor>()
    private val processorLock = ReentrantLock()

    /**
     * Put the pair of topic name and EventProcessor to the HashMap.
     * Before registration check topic is already exist in the hash map.
     * If specified topic is empty, make new PublishProcessor and serialize it
     * for thread-safety.
     *
     * @param   eventProcessor      Topic and Processor info holder. made by EventPressBuilder
     * @return  EventProcessor
     */

    fun registerEventProcessor(eventProcessor: EventProcessor): EventProcessor {
        return LockUtil.lock(processorLock) {
            var result = processorMap[eventProcessor.topic.topic]
            if(result == null) {
                // create processor
                eventProcessor.setProcessor(PublishProcessor.create<Any>().toSerialized())
                // add to hash map
                processorMap[eventProcessor.topic.topic] = eventProcessor
                result = eventProcessor
            }
            if(DebugUtil.isDebugMode())
                DebugUtil.printTopics(processorMap.keys())

            result
        }
    }

    /**
     * @Deprecated  Remove must work in recursive manner.
     * Removes single topic and EventProcessor pair from hash map.
     */
    @Deprecated("Not available. Use removeEventProcessors() instead.", ReplaceWith("removeEventProcessors(topic)"))
    fun removeEventProcessor(topic: String): Boolean {
        return LockUtil.lock(processorLock) {
            val removed = processorMap.remove(topic)?.apply {
                // dispose connected disposables and stop processor
                stopProcessor()
            }
            if(DebugUtil.isDebugMode())
                DebugUtil.printTopics(processorMap.keys())

            removed != null
        }
    }

    /**
     * Removes specified topic and descendants.
     * All subscriber on this processor would receive onComplete message.
     *
     * @param   topic       topic string.
     */
    fun removeEventProcessors(topic: String) {
        LockUtil.lock(processorLock) {
            searchEventProcessors(topic).forEach {
                processorMap.remove(it.key)
                // dispose connected disposables and stop processor
                it.value.stopProcessor()
            }
            if(DebugUtil.isDebugMode())
                DebugUtil.printTopics(processorMap.keys())
        }
    }

    /**
     * Search the hash map with topic string and returns an EventProcessor
     *
     * @param   topic       topic string.
     * @return  EventProcessor?
     */
    fun getProcessor(topic: String): EventProcessor? {
        val eventProcessor = searchEventProcessor(topic)
        if(eventProcessor?.getProcessor() != null)
            return eventProcessor
        return null
    }

    /**
     * Publish event to the topic and descendants.
     * To publish to all the descendants, set recursive param as true
     *
     * @param   topic       topic string.
     */
    fun publishEvent(topic: String, contents: Any, recursive: Boolean = false) {
        if(recursive) {
            // publish event to target topic and descendants
            searchEventProcessors(topic).forEach {
                DebugUtil.printPublishEvent(it.key, it.value, true)
                it.value.getProcessor()?.apply {
                    if(hasSubscribers()) {
                        onNext(contents)
                    }
                }
            }
        } else {
            // publish event to the target topic only
            processorMap[topic]?.apply {
                DebugUtil.printPublishEvent(topic, this)
                getProcessor()?.apply {
                    if(hasSubscribers()) {
                        onNext(contents)
                    }
                }
            }
        }
    }

    /**
     * Search hash map and returns single EventProcessor
     * which is exactly match with topic string.
     *
     * @param   topic       topic string.
     * @return  EventProcessor
     */
    private fun searchEventProcessor(topic: String): EventProcessor? {
        return processorMap[topic]
    }

    /**
     * Search hash map with topic string and returns target processor
     * and his descendants.
     *
     * @param   topic       topic string.
     * @return  Map<String, EventProcessor>
     */
    private fun searchEventProcessors(topic: String): Map<String, EventProcessor> {
        return processorMap.filterKeys { it.startsWith(topic) }
    }

}
