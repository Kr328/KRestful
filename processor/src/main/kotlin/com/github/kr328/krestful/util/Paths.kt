package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.UrlTemplate

private val PATH_PLACEHOLDER_PATTERN = Regex("\\{[a-zA-Z0-9]+}")

fun String.parsePath(placeholders: Map<String, String>): UrlTemplate {
    val segments = sequence {
        var begin = 0

        PATH_PLACEHOLDER_PATTERN.findAll(this@parsePath).forEach {
            yield(UrlTemplate.Segment.Literal(substring(begin until it.range.first)))

            val placeholder = it.value.removeSurrounding("{", "}")
            val argument =
                placeholders[placeholder] ?: throw IllegalArgumentException("Argument of $placeholder not found")

            yield(UrlTemplate.Segment.Variable(argument, placeholder))

            begin = it.range.last + 1
        }

        yield(UrlTemplate.Segment.Literal(substring(begin until length)))
    }.filterNot { it is UrlTemplate.Segment.Literal && it.value.isEmpty() }.toList()

    return UrlTemplate(segments)
}