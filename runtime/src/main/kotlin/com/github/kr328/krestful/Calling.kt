package com.github.kr328.krestful

import io.ktor.server.application.*
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

class Calling(val call: ApplicationCall) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = Key

    companion object Key : CoroutineContext.Key<Calling> {
        suspend inline fun get(): Calling? {
            return currentCoroutineContext()[Key]
        }

        suspend inline fun require(): Calling {
            return get()!!
        }
    }
}
