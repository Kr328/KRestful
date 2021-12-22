package com.github.kr328.krestful.model

import kotlinx.serialization.Serializable

@Serializable
data class Version(val version: String, val premium: Boolean = false)