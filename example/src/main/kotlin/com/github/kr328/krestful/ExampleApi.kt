package com.github.kr328.krestful

import com.github.kr328.krestful.annotations.*
import com.github.kr328.krestful.model.Proxy
import com.github.kr328.krestful.model.Traffic
import io.ktor.http.content.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

@Restful
interface ExampleApi {
    @GET("/json")
    suspend fun json(): TextContent

    @GET("/")
    suspend fun ping(): TextContent

    @WebSocket
    @GET("/traffic")
    fun traffic(): Flow<Traffic>

    @GET("/stub")
    suspend fun stub(@Field name: String, @Field obj: JsonObject)

    @GET("/proxies")
    suspend fun proxies(): Proxy.All

    @GET("/proxies/{name}")
    suspend fun proxy(@Path name: String): Proxy
}
