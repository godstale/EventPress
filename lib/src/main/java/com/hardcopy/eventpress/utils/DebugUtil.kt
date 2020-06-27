package com.hardcopy.eventpress.utils

import android.util.Log
import com.hardcopy.eventpress.eventbus.EventProcessor
import org.reactivestreams.Processor
import java.util.*

/**
 * DebugUtil has log and dump tools.
 * User can switch the log with xxxPrint params.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-15.
 */
object DebugUtil {
    private var debugMode = false
    private var publishPrint = false
    private var observePrint = false
    private var topicPrint = false
    private var processorPrint = false

    /**
     * If debug mode is off, EventPress keeps silence.
     *
     * @param   isOn
     */
    fun setDebugMode(isOn: Boolean) {
        debugMode = isOn
    }

    fun isDebugMode(): Boolean {
        return debugMode
    }

    /**
     * Print or not when user publish event to any topic.
     *
     * @param   isOn
     */
    fun usePublishLog(isOn: Boolean) {
        publishPrint = isOn
    }

    /**
     * Print or not when user observes any topic. (Not available yet)
     *
     * @param   isOn
     */
    fun useObserveLog(isOn: Boolean) {
        observePrint = isOn
    }

    /**
     * Print changed topic list when user registers/removes a topic.
     *
     * @param   isOn
     */
    fun useTopicDump(isOn: Boolean) {
        topicPrint = isOn
    }

    /**
     * Print list of EventProcessor info. (Not available yet)
     */
    fun useProcessorDump(isOn: Boolean) {
        processorPrint = isOn
    }

    fun printPublishEvent(topic: String, targetProcessor: EventProcessor? = null, recursive: Boolean = false) {
        if(publishPrint) {
            val count = targetProcessor?.getDisposableCount() ?: 0
            val isRecursive = if(recursive) " Recursive ::" else ""
            Log.d("###", "    --->$isRecursive send event to [$topic], target count = $count")
        }
    }

    fun printObserveEvent(topic: String) {
        if(observePrint) {
            Log.d("###", "    <--- [$topic] received event")
        }
    }

    fun printTopics(topics: Enumeration<String>) {
        if(topicPrint) {
            Log.d("###", " ")
            Log.d("###", "---------- Topic list ----------")
            topics.iterator().forEach {
                Log.d("###", it)
            }
            Log.d("###", "---------- End ----------")
            Log.d("###", " ")
        }
    }

    fun printEventProcessors(processors: Enumeration<EventProcessor>) {
        if(processorPrint) {
            Log.d("###", " ")
            Log.d("###", "---------- Processor list ----------")
            processors.iterator().forEach {
                Log.d("###", it.toString())
            }
            Log.d("###", "---------- End ----------\n")
            Log.d("###", " ")
        }
    }
}