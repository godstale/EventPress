package com.hardcopy.eventpress.exceptions

import java.lang.RuntimeException

/**
 * Created by godstale@hotmail.com(Suh Young-bae) on 06.15.2020.
 */

class EventPressNotInitializedException(
        message: String = "EventPress is not initialized. Call EventPress.initialize() first.")
    : RuntimeException(message) {}