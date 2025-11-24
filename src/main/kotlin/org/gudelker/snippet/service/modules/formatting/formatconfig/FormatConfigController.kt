package org.gudelker.snippet.service.modules.formatting.formatconfig

import org.gudelker.snippet.service.modules.formatting.FormattingOrchestratorService
import org.gudelker.snippet.service.modules.formatting.formatconfig.input.ActivateFormatRuleRequest
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
@RequestMapping("/formatconfig")
class FormatConfigController(
    private val formatConfigService: FormatConfigService,
    private val orchestratorFormattingservice: FormattingOrchestratorService,
) {
    private val logger = LoggerFactory.getLogger(FormatConfigController::class.java)

    @GetMapping("/user")
    fun getLintConfigsByUserId(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<FormatConfig> {
        logger.info("Fetching format rules for user: {}", jwt.subject)
        val configs = formatConfigService.getAllRulesFromUser(jwt.subject)
        logger.info("Retrieved {} format rules for user: {}", configs.size, jwt.subject)
        return configs
    }

    @PostMapping
    fun modifyRule(
        @RequestBody request: ActivateFormatRuleRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<FormatConfig> {
        logger.info("User: {} modifying format rule: {}", jwt.subject, request)
        val result = formatConfigService.modifyFormatConfig(request, jwt.subject)

        return if (result != null) {
            logger.info("Successfully modified format rule for user: {}", jwt.subject)
            orchestratorFormattingservice.formatUserSnippets(jwt.subject)
            logger.info("Triggered formatting for all snippets of user: {}", jwt.subject)
            ResponseEntity.ok(result)
        } else {
            logger.warn("Format rule modification returned null for user: {}", jwt.subject)
            ResponseEntity.noContent().build()
        }
    }
}
