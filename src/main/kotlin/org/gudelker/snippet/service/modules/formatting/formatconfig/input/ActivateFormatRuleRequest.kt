package org.gudelker.snippet.service.modules.formatting.formatconfig.input

data class ActivateFormatRuleRequest(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val ruleValue: Int?,
    val hasValue: Boolean,
)
