package com.hardcopy.eventpress.exceptions

import java.lang.RuntimeException

/**
 * Created by godstale@hotmail.com(Suh Young-bae) on 06.15.2020.
 */

enum class EventPressException(val exception: RuntimeException) {
    NOT_INITIALIZED(EventPressNotInitializedException()),
    OBSERVE_FAILED(EventPressNotObservableException()),
    TYPE_CASTING_FAILED(EventPressTypeCastingException())
}