package org.gudelker.snippet.service.modules.formatting.formatconfig

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FormatConfigRepository : JpaRepository<FormatConfig, UUID> {
    fun findByUserId(userId: String): List<FormatConfig>

    fun findByUserIdAndFormatRuleId(
        userId: String,
        ruleId: UUID,
    ): FormatConfig?
}
