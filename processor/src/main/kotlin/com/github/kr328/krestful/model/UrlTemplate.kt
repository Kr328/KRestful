package com.github.kr328.krestful.model

data class UrlTemplate(val segments: List<Segment>) {
    sealed class Segment {
        data class Variable(val name: String, val key: String) : Segment()
        data class Literal(val value: String) : Segment()
    }
}