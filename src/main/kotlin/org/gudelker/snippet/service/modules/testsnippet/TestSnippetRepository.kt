package org.gudelker.snippet.service.modules.testsnippet

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TestSnippetRepository : JpaRepository<TestSnippet, UUID> {
    fun findAllBySnippetId(snippetId: UUID): List<TestSnippet>
}
