// src/main/kotlin/org/gudelker/snippet/service/orchestrator/FormattingOrchestratorService.kt
package org.gudelker.snippet.service.modules.formatting

import org.gudelker.snippet.service.modules.formatting.formatconfig.FormatConfigService
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRuleRepository
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.snippets.dto.FormatRuleNameWithValue
import org.gudelker.snippet.service.redis.dto.FormatRequest
import org.gudelker.snippet.service.redis.producer.FormatPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@Service
class FormattingOrchestratorService(
    private val snippetRepository: SnippetRepository,
    private val formatConfigService: FormatConfigService,
    private val formatRuleRepository: FormatRuleRepository,
    private val formatPublisher: FormatPublisher,
) {
    private fun formatSnippets(
        snippetIds: List<UUID>,
        formatRules: List<FormatRuleNameWithValue>,
    ) {
        val defaultFormatRules = formatRuleRepository.findAll()
        val lintRulesNames = defaultFormatRules.map { it.name }
        for (snippetId in snippetIds) {
            try {
                val snippet = snippetRepository.findById(snippetId)
                val version = snippet.get().languageVersion.version
                val req =
                    FormatRequest(
                        snippetId = snippetId.toString(),
                        snippetVersion = version,
                        userRules = formatRules,
                        allRules = lintRulesNames,
                    )
                println("Publishing lint request to Redis: $req") // Add this line
                formatPublisher.publishFormatRequest(req)
                println("Published lint request to Redis for snippet $snippetId") // And this
            } catch (err: Exception) {
                throw HttpClientErrorException(HttpStatus.NOT_FOUND, "snippet ID is missing in JWT")
            }
        }
    }

    fun formatSingleSnippet(
        snippetId: UUID,
        userId: String,
    ): String {
        val userFormatRules = formatConfigService.getAllRulesFromUser(userId)
        val rulesWithValue =
            userFormatRules.map { formatConfig ->
                FormatRuleNameWithValue(
                    ruleName = formatConfig.formatRule?.name ?: "",
                    value = formatConfig.ruleValue ?: 0,
                )
            }
        formatSnippets(listOf(snippetId), rulesWithValue)
        return "Formatting request published for snippet $snippetId"
    }

    fun formatUserSnippets(userId: String) {
        val snippetsIds = snippetRepository.findByOwnerId(userId).mapNotNull { it.id }
        val userFormatRules = formatConfigService.getAllRulesFromUser(userId)
        val rulesWithValue =
            userFormatRules.map { formatConfig ->
                FormatRuleNameWithValue(
                    ruleName = formatConfig.formatRule?.name ?: "",
                    value = formatConfig.ruleValue ?: 0,
                )
            }
        formatSnippets(snippetsIds, rulesWithValue)
    }
}
