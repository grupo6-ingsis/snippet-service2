package org.gudelker.snippet.service.snippets

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SnippetRepository : JpaRepository<Snippet, UUID> {
    fun findByUserId(userId: String): List<Snippet>
}