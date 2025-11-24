package org.gudelker.snippet.service.modules.linting.lintconfig

import org.gudelker.snippet.service.modules.linting.LintingOrchestratorService
import org.gudelker.snippet.service.modules.linting.lintconfig.input.ActivateRuleRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lintconfig")
class LintConfigController(
    private val lintConfigService: LintConfigService,
    private val orchestratorLintingService: LintingOrchestratorService,
) {
    private val logger = LoggerFactory.getLogger(LintConfigController::class.java)

    @GetMapping("/user")
    fun getLintConfigsByUserId(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<LintConfig> {
        logger.info("Fetching lint rules for user: {}", jwt.subject)
        val configs = lintConfigService.getAllRulesFromUser(jwt.subject)
        logger.info("Retrieved {} lint rules for user: {}", configs.size, jwt.subject)
        return configs
    }

    @PostMapping
    fun modifyRule(
        @RequestBody request: ActivateRuleRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<LintConfig> {
        logger.info("User: {} modifying lint rule: {}", jwt.subject, request)
        val result = lintConfigService.modifyRule(request, jwt.subject)

        return if (result != null) {
            logger.info("Successfully modified lint rule for user: {}", jwt.subject)
            orchestratorLintingService.lintUserSnippets(jwt.subject)
            logger.info("Triggered linting for all snippets of user: {}", jwt.subject)
            ResponseEntity.ok(result)
        } else {
            logger.warn("Lint rule modification returned null for user: {}", jwt.subject)
            ResponseEntity.noContent().build()
        }
    }
}
