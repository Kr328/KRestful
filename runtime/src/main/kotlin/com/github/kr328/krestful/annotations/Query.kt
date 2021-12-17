package com.github.kr328.krestful.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(
    val key: String = "",
)
