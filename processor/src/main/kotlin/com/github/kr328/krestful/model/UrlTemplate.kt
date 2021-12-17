package com.github.kr328.krestful.model

data class UrlTemplate(val segments: List<Segment>) {
    sealed class Segment {
        data class Placeholder(val key: String) : Segment()
        data class Literal(val value: String) : Segment()
    }
}