package org.gudelker.snippet.service.modules.snippets.dto.get

import org.gudelker.snippet.service.modules.langver.LanguageVersion
import org.gudelker.snippet.service.modules.snippets.dto.types.ComplianceType
import java.time.OffsetDateTime

data class SnippetWithComplianceDto(
    val id: String,
    val title: String,
    val description: String?,
    val ownerId: String,
    val languageVersion: LanguageVersion,
    val created: OffsetDateTime,
    val updated: OffsetDateTime,
    val compliance: ComplianceType,
)
