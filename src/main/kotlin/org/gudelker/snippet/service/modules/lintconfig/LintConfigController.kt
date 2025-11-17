package org.gudelker.snippet.service.modules.lintconfig

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lintconfig")
class LintConfigController(private val lintConfigService: LintConfigService) {
    @GetMapping("/user")
    fun getLintConfigsByUserId(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<LintConfig> {
        return lintConfigService.getAllRulesFromUser(jwt.subject)
    }
}
