package org.gudelker.snippet.service.modules.lintresult

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LintResultRepository : JpaRepository<LintResult, UUID>
