package com.hardcopy.eventpress.utils

/**
 * String toolkit.
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-15.
 */
object StringUtil {
    // for topic string validation check
    val topicRegEx = "^[._0-9a-zA-Z/-]*$"
    // check not visible characters
    val emptyCharRegEx = "\\p{Z}"
}