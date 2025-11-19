package org.gudelker.snippet.service.modules.formatting.formatconfig

import org.gudelker.snippet.service.modules.formatting.formatconfig.input.ActivateFormatRuleRequest
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
class FormatConfigController(private val formatConfigService: FormatConfigService) {
    @GetMapping("/user")
    fun getLintConfigsByUserId(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<FormatConfig> {
        return formatConfigService.getAllRulesFromUser(jwt.subject)
    }

    @PostMapping
    fun modifyRule(
        @RequestBody request: ActivateFormatRuleRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<FormatConfig> {
        val result = formatConfigService.modifyFormatConfig(request, jwt.subject)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.noContent().build()
        }
    }
}
