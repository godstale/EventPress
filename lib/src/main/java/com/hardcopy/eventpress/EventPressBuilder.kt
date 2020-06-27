package com.hardcopy.eventpress

import com.hardcopy.eventpress.eventbus.*

/**
 * Builder class to make new topic with various options.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-07.
 */
class EventPressBuilder(private val eventBus: EventBus) {
    companion object {
        fun create(eventBus: EventBus): EventPressBuilder {
            return EventPressBuilder(eventBus)
        }
    }

    private var topicString: String = ""
    private var schedulerType = EventScheduler.Type.COMPUTATION
    private var backpressureType = EventFlowControl.BpType.BUFFER
    private var useValve = false

    // topic is fixed by setClassTopic() or setUiTopic()
    private var topicPinned = false
    // scheduler is fixed as UI thread by setUiTopic()
    private var schedulerPinned = false

    /**
     * Set topic path string.
     *
     * @param   topicPath
     * @return  EventPressBuilder
     */
    fun setTopic(topicPath: String): EventPressBuilder {
        if(!topicPinned) {
            topicString = topicPath
        }
        return this
    }

    /**
     * Set topic with class type. EventPress converts class type to topic path string.
     * ex> com.exam.event.MainActivity -> /sys/class/com.exam.event.MainActivity
     *
     * @param   clazz
     * @return  EventPressBuilder
     */
    fun setTopic(clazz: Class<*>): EventPressBuilder {
        if(!topicPinned) {
            topicString = EventTopic.TOPIC_PATH_CLASS + clazz.canonicalName.toString()
            topicPinned = true
        }
        return this
    }

    /**
     * Set backpressure strategy. Default is EventFlowControl.BpType.BUFFER.
     *
     * @param   type
     * @return  EventPressBuilder
     */
    fun withBackpressure(type: EventFlowControl.BpType): EventPressBuilder {
        backpressureType = type
        return this
    }

    /**
     * Attack valve to topic stream.
     *
     * @return  EventPressBuilder
     */
    fun withValve(): EventPressBuilder {
        useValve = true
        return this
    }

    /**
     * Set scheduler on topic stream. Default is EventScheduler.Type.COMPUTATION.
     * If you wish to receive event in UI thread, use EventScheduler.Type.UI.
     *
     * @param   type
     * @return  EventPressBuilder
     */
    fun withScheduler(type: EventScheduler.Type): EventPressBuilder {
        if(!schedulerPinned) {
            schedulerType = type
        }
        return this
    }

    /**
     * Make EventProcessor instance and hand over to EventBus to make Rx stream, PublishProcessor.
     *
     * @return  Boolean
     */
    fun build(): Boolean {
        // Check topic validation
        if(!EventTopic.isValidForRegister(topicString)) return false

        // Make flow controller
        val eventFlowControl = EventFlowControl(backpressureType, useValve)
        // Make scheduler
        val eventScheduler = EventScheduler(schedulerType)

        EventProcessor(EventTopic(topicString), eventScheduler, eventFlowControl).let {
            eventBus.registerEventProcessor(it)
        }
        return true
    }

    /**
     * Not available yet
     */
    fun withIdleCaching(): EventPressBuilder {
        // TODO:
        return this
    }
}