package com.github.kr328.krestful

import com.github.kr328.krestful.annotations.*
import com.github.kr328.krestful.model.Configs
import com.github.kr328.krestful.model.Proxy
import com.github.kr328.krestful.model.Traffic
import kotlinx.coroutines.flow.Flow

@Restful
interface ExampleApi {
    @GET("/")
    suspend fun ping(): String

    @GET("/configs")
    suspend fun getConfigs(): Configs

    @PATCH("/configs")
    suspend fun patchConfigs(@Body configs: Configs)

    @GET("/proxies")
    suspend fun proxies(): Proxy.All

    @GET("/proxies/{name}")
    suspend fun proxy(@Path name: String): Proxy

    @WebSocket("/traffic")
    fun traffic(): Flow<Traffic>

    @POST("/create/proxy")
    suspend fun createProxy(@Field name: String, @Field port: Int): Proxy

    @GET("/headers")
    suspend fun headers(@Header("Example-Header") example: String, @Header name: String): String

    @GET("/queries")
    suspend fun queries(@Query name: String, @Query("ss") server: String): String

    @WebSocket("/echo")
    fun echo(@Outgoing input: Flow<String>): Flow<String>

    @POST("/nullable")
    suspend fun nullable(@Query query: String?, @Header header: String?, @Field field: String?): String
}
