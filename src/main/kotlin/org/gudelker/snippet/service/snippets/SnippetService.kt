package org.gudelker.snippet.service.snippets

import org.springframework.stereotype.Service

@Service
class SnippetService (private val snippetRepository: SnippetRepository) {

    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippet(userId: String, title: String, content: String): Snippet {
        val snippet = Snippet(userId = userId, title = title, content = content)
        return snippetRepository.save(snippet)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByUserId(userId)
    }
}