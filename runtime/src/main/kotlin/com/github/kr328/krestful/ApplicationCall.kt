package com.github.kr328.krestful

import kotlin.coroutines.CoroutineContext
import io.ktor.application.ApplicationCall as KApplicationCall

class ApplicationCall(val delegate: KApplicationCall) : CoroutineContext.Element, KApplicationCall by delegate {
    override val key: CoroutineContext.Key<*>
        get() = Key

    companion object Key : CoroutineContext.Key<ApplicationCall>
}
