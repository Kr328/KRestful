package com.github.kr328.krestful

import io.ktor.http.*

sealed class Content {
    abstract val contentType: ContentType
    abstract val bytes: ByteArray

    data class Text(val text: String, override val contentType: ContentType) : Content() {
        override val bytes: ByteArray
            get() {
                return text.toByteArray(contentType.charset() ?: Charsets.UTF_8)
            }
    }

    data class Binary(
        override val bytes: ByteArray,
        override val contentType: ContentType = ContentType.Any
    ) : Content() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Binary

            if (!bytes.contentEquals(other.bytes)) return false
            if (contentType != other.contentType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + contentType.hashCode()
            return result
        }
    }
}