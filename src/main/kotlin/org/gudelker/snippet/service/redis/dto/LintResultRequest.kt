package org.gudelker.snippet.service.redis.dto

data class LintResultRequest(
    val message: String,
    val line: Number,
    val column: Number,
)
