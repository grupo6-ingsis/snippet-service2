// src/main/kotlin/org/gudelker/snippet/service/orchestrator/LintingOrchestratorService.kt
package org.gudelker.snippet.service.modules.linting

import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.linting.lintrule.LintRuleRepository
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.snippets.dto.RuleNameWithValue
import org.gudelker.snippet.service.redis.dto.LintRequest
import org.gudelker.snippet.service.redis.producer.LintPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@Service
class LintingOrchestratorService(
    private val snippetRepository: SnippetRepository,
    private val lintConfigService: LintConfigService,
    private val lintRuleRepository: LintRuleRepository,
    private val lintPublisher: LintPublisher,
) {
    private fun lintSnippets(
        snippetIds: List<UUID>,
        lintRules: List<RuleNameWithValue>,
    ) {
        println("------------------------------------------ENTRO A LINTSNIPPETS")
        val defaultLintRules = lintRuleRepository.findAll()
        val lintRulesNames = defaultLintRules.map { it.name }
        for (snippetId in snippetIds) {
            try {
                val snippet = snippetRepository.findById(snippetId)
                val version = snippet.get().languageVersion.version
                val req =
                    LintRequest(
                        snippetId = snippetId.toString(),
                        snippetVersion = version,
                        userRules = lintRules,
                        allRules = lintRulesNames,
                    )
                println("Publishing lint request to Redis: $req") // Add this line
                lintPublisher.publishLintRequest(req)
                println("Published lint request to Redis for snippet $snippetId") // And this
            } catch (err: Exception) {
                throw HttpClientErrorException(HttpStatus.NOT_FOUND, "snippet ID is missing in JWT")
            }
        }
    }

    fun lintSingleSnippet(
        snippetId: UUID,
        userId: String,
    ) {
        val userLintRules = lintConfigService.getAllRulesFromUser(userId)
        val rulesWithValue =
            userLintRules.map { lintConfig ->
                RuleNameWithValue(
                    ruleName = lintConfig.lintRule?.name ?: "",
                    value = lintConfig.ruleValue ?: "",
                )
            }
        lintSnippets(listOf(snippetId), rulesWithValue)
    }

    fun lintUserSnippets(userId: String) {
        println("---------------------------------------$userId")
        val snippetsIds = snippetRepository.findByOwnerId(userId).mapNotNull { it.id }
        println("---------------------------------------$snippetsIds")
        val userLintRules = lintConfigService.getAllRulesFromUser(userId)
        println("---------------------------------------$userLintRules")
        val rulesWithValue =
            userLintRules.map { lintConfig ->
                RuleNameWithValue(
                    ruleName = lintConfig.lintRule?.name ?: "",
                    value = lintConfig.ruleValue ?: "",
                )
            }
        println("---------------------------------------$rulesWithValue")
        lintSnippets(snippetsIds, rulesWithValue)
    }
}
