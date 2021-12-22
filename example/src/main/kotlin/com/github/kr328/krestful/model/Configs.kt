package com.github.kr328.krestful.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Configs(
    @SerialName("port")
    val port: Int,
    @SerialName("socks-port")
    val socksPort: Int,
    @SerialName("redir-host")
    val redirPort: Int,
    @SerialName("tproxy-port")
    val tproxyPort: Int,
    @SerialName("mixed-port")
    val mixedPort: Int,
    @SerialName("allow-lan")
    val allowLan: Boolean,
    @SerialName("mode")
    val mode: String,
    @SerialName("log-level")
    val logLevel: String,
    @SerialName("ipv6")
    val ipv6: Boolean,
)