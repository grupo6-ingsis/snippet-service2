package org.gudelker.snippet.service.modules.snippets.service

import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.SnippetDtoResponse
import org.gudelker.snippet.service.modules.snippets.input.CreateSnippetInput
import org.gudelker.snippet.service.modules.snippets.repository.SnippetRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class SnippetService (private val snippetRepository: SnippetRepository) {

    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippet(input: CreateSnippetInput, userId: String): SnippetDtoResponse {
        val snippet = Snippet(userId = userId, title = input.title, content = input.content, language = input.language ,  created = OffsetDateTime.now(), updated = OffsetDateTime.now())
        snippetRepository.save(snippet)
        return createSnippetResponse(input,userId)
    }
    private fun createSnippetResponse(input: CreateSnippetInput, userId: String): SnippetDtoResponse{
        return SnippetDtoResponse(input.title,input.content,userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByUserId(userId)
    }


}