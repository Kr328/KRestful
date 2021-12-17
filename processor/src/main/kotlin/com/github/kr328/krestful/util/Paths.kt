package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.UrlTemplate

private val PATH_PLACEHOLDER_PATTERN = Regex("\\{[a-zA-Z0-9]+}")

fun String.parsePath(): UrlTemplate {
    val segments = sequence {
        var begin = 0

        PATH_PLACEHOLDER_PATTERN.findAll(this@parsePath).forEach {
            yield(UrlTemplate.Segment.Literal(substring(begin until it.range.first)))

            yield(UrlTemplate.Segment.Placeholder(it.value.removeSurrounding("{", "}")))

            begin = it.range.last + 1
        }

        yield(UrlTemplate.Segment.Literal(substring(begin until length)))
    }.filterNot { it is UrlTemplate.Segment.Literal && it.value.isEmpty() }.toList()

    return UrlTemplate(segments)
}