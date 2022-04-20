package com.github.kr328.krestful

import io.ktor.http.*

class RemoteException(
    val status: HttpStatusCode,
    val extras: Map<String, String> = emptyMap(),
) : Exception()
