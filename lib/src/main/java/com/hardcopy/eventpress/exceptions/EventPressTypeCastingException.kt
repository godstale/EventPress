package com.hardcopy.eventpress.exceptions

import java.lang.RuntimeException

/**
 * Created by godstale@hotmail.com(Suh Young-bae) on 06.15.2020.
 */

class EventPressTypeCastingException(
        message: String = "Type casting failed.")
    : RuntimeException(message) {}