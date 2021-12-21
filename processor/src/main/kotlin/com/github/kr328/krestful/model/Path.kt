package com.github.kr328.krestful.model

class Path(segments: List<Segment>) : List<Path.Segment> by segments {
    sealed class Segment {
        data class Variable(val name: String, val key: String) : Segment()
        data class Literal(val value: String) : Segment()
    }
}
