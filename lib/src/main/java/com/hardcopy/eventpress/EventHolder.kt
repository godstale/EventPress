package com.hardcopy.eventpress

/**
 * Use this as a wrapper for data that is exposed via a EventPress that represents an event.
 * Refer to: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 06.15.2020.
 */
open class EventHolder<out T>(private val eventType: Int, private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content

    /**
     * Returns the event type
     */
    fun getEventType(): Int = eventType
}