package com.github.kr328.krestful.util

import com.github.kr328.krestful.model.Path

private val PATH_PLACEHOLDER_PATTERN = Regex("\\{[a-zA-Z0-9]+}")

fun String.parsePath(placeholders: Map<String, String>): Path {
    val segments = sequence {
        var begin = 0

        PATH_PLACEHOLDER_PATTERN.findAll(this@parsePath).forEach {
            yield(Path.Segment.Literal(substring(begin until it.range.first)))

            val placeholder = it.value.removeSurrounding("{", "}")
            val argument = placeholders[placeholder] ?: error("Argument of $placeholder not found")

            yield(Path.Segment.Variable(argument, placeholder))

            begin = it.range.last + 1
        }

        yield(Path.Segment.Literal(substring(begin until length)))
    }.filterNot { it is Path.Segment.Literal && it.value.isEmpty() }.toList()

    return Path(segments)
}