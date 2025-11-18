package org.gudelker.snippet.service.redis.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.gudelker.snippet.service.modules.snippets.dto.RuleNameWithValue

data class LintRequest
@JsonCreator
constructor(
    @JsonProperty("snippetId") val snippetId: String,
    @JsonProperty("snippetVersion") val snippetVersion: String,
    @JsonProperty("userRules") val userRules: List<RuleNameWithValue>,
    @JsonProperty("allRules") val allRules: List<String>,
    @JsonProperty("requestedAt") val requestedAt: Long = System.currentTimeMillis(),
)
