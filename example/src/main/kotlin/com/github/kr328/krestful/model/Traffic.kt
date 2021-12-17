package com.github.kr328.krestful.model

import kotlinx.serialization.Serializable

@Serializable
data class Traffic(val up: Long, val down: Long)
