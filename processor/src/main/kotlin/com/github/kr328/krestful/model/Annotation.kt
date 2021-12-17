package com.github.kr328.krestful.model

import com.squareup.kotlinpoet.ClassName

data class Annotation(val type: ClassName, val values: Map<String, Any?>)
