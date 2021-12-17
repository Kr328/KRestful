package com.github.kr328.krestful.util

inline fun String?.ifNullOrEmpty(block: () -> String): String {
    return (this ?: "").ifEmpty(block)
}
