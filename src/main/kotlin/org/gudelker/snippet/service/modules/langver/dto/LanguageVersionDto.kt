package org.gudelker.snippet.service.modules.langver.dto

data class LanguageVersionDto(
    val versions: List<String>,
    val languageName: String,
    val extension: String,
)
