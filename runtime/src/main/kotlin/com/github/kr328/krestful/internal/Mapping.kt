package com.github.kr328.krestful.internal

import com.github.kr328.krestful.Content
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

sealed class Mapping<T> {
    companion object {
        private val EmptyContent = Content.Binary(byteArrayOf())
    }

    abstract fun mappingToValue(content: Content): T
    abstract fun mappingToContent(value: T): Content

    object Unit : Mapping<kotlin.Unit>() {
        override fun mappingToValue(content: Content) {

        }

        override fun mappingToContent(value: kotlin.Unit): Content {
            return EmptyContent
        }
    }

    object Int : Mapping<kotlin.Int>() {
        override fun mappingToValue(content: Content): kotlin.Int {
            return ContentText.mappingToValue(content).text.toInt()
        }

        override fun mappingToContent(value: kotlin.Int): Content {
            return Content.Text(value.toString(), ContentType.Text.Plain)
        }
    }

    object Long : Mapping<kotlin.Long>() {
        override fun mappingToValue(content: Content): kotlin.Long {
            return ContentText.mappingToValue(content).text.toLong()
        }

        override fun mappingToContent(value: kotlin.Long): Content {
            return Content.Text(value.toString(), ContentType.Text.Plain)
        }
    }

    object Float : Mapping<kotlin.Float>() {
        override fun mappingToValue(content: Content): kotlin.Float {
            return ContentText.mappingToValue(content).text.toFloat()
        }

        override fun mappingToContent(value: kotlin.Float): Content {
            return Content.Text(value.toString(), ContentType.Text.Plain)
        }
    }

    object Double : Mapping<kotlin.Double>() {
        override fun mappingToValue(content: Content): kotlin.Double {
            return ContentText.mappingToValue(content).text.toDouble()
        }

        override fun mappingToContent(value: kotlin.Double): Content {
            return Content.Text(value.toString(), ContentType.Text.Plain)
        }
    }

    object Boolean : Mapping<kotlin.Boolean>() {
        override fun mappingToValue(content: Content): kotlin.Boolean {
            return ContentText.mappingToValue(content).text.toBoolean()
        }

        override fun mappingToContent(value: kotlin.Boolean): Content {
            return Content.Text(value.toString(), ContentType.Text.Plain)
        }
    }

    object String : Mapping<kotlin.String>() {
        override fun mappingToValue(content: Content): kotlin.String {
            return ContentText.mappingToValue(content).text
        }

        override fun mappingToContent(value: kotlin.String): Content {
            return Content.Text(value, ContentType.Any)
        }
    }

    object ByteArray : Mapping<kotlin.ByteArray>() {
        override fun mappingToValue(content: Content): kotlin.ByteArray {
            return ContentBinary.mappingToValue(content).bytes
        }

        override fun mappingToContent(value: kotlin.ByteArray): Content {
            return Content.Binary(value, ContentType.Any)
        }
    }

    object ContentText : Mapping<Content.Text>() {
        override fun mappingToValue(content: Content): Content.Text {
            return when (content) {
                is Content.Binary -> Content.Text(
                    content.bytes.toString(content.contentType.charset() ?: Charsets.UTF_8),
                    content.contentType
                )
                is Content.Text -> content
            }
        }

        override fun mappingToContent(value: Content.Text): Content {
            return value
        }
    }

    object ContentBinary : Mapping<Content.Binary>() {
        override fun mappingToValue(content: Content): Content.Binary {
            return when (content) {
                is Content.Binary -> content
                is Content.Text -> Content.Binary(content.bytes, content.contentType)
            }
        }

        override fun mappingToContent(value: Content.Binary): Content {
            return value
        }
    }

    class SerializableJson<T>(val json: Json, val serializer: KSerializer<T>) : Mapping<T>() {
        override fun mappingToValue(content: Content): T {
            if (content.contentType != ContentType.Application.Json && content.contentType != ContentType.Any) {
                throw SerializationException("Unsupported Content-Type: ${content.contentType}")
            }

            val text = when (content) {
                is Content.Binary -> ContentText.mappingToValue(content).text
                is Content.Text -> content.text
            }

            return json.decodeFromString(serializer, text)
        }

        override fun mappingToContent(value: T): Content {
            return Content.Text(json.encodeToString(serializer, value), ContentType.Application.Json)
        }
    }
}
