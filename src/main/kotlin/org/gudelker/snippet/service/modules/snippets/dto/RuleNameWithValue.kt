package org.gudelker.snippet.service.modules.snippets.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RuleNameWithValue
    @JsonCreator
    constructor(
        @JsonProperty("ruleName") val ruleName: String,
        @JsonProperty("value") val value: String,
    )
