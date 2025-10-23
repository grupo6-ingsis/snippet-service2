package org.gudelker.snippet.service.modules.snippets.repository

import org.gudelker.snippet.service.modules.snippets.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SnippetRepository : JpaRepository<Snippet, UUID> {
    fun findByUserId(userId: String): List<Snippet>
}