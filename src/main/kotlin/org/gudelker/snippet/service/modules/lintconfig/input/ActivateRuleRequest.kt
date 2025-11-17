package org.gudelker.snippet.service.modules.lintconfig.input

data class ActivateRuleRequest(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val ruleValue: String?,
    val hasValue: Boolean,
)
