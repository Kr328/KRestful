package com.github.kr328.krestful.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class WebSocket(
    val path: String
)
