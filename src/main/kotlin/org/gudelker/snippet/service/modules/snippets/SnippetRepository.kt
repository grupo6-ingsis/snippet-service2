package org.gudelker.snippet.service.modules.snippets

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SnippetRepository : JpaRepository<Snippet, UUID> {
    fun findByOwnerId(userId: String): List<Snippet>
}