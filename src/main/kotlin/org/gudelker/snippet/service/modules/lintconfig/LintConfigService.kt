package org.gudelker.snippet.service.modules.lintconfig

import org.springframework.stereotype.Service

@Service
class LintConfigService(private val lintConfigRepository: LintConfigRepository) {
    fun getAllRulesFromUser(userId: String): List<LintConfig> {
        return lintConfigRepository.findByUserId(userId)
    }
}
