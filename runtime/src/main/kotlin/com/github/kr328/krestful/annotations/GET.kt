package com.github.kr328.krestful.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GET(
    val path: String,
)
