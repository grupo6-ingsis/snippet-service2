package org.gudelker.snippet.service.modules.snippets.service

import org.gudelker.snippet.service.modules.permissions.repository.PermissionRepository
import org.gudelker.snippet.service.modules.permissions.service.PermissionService
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.repository.SnippetRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class SnippetService (private val snippetRepository: SnippetRepository, private val permissionService: PermissionService) {

    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippetFromFile(input: CreateSnippetFromFileInput, userId: String): SnippetFromFileResponse {
        val permissions = permissionService.getAllPermissions().toMutableSet()
        val snippet = Snippet(ownerId = userId, title = input.title, content = input.description, language = input.language ,  created = OffsetDateTime.now(), updated = OffsetDateTime.now(), permissions = permissions)
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input,userId)
    }
    private fun createSnippetFromFileResponse(input: CreateSnippetFromFileInput, userId: String): SnippetFromFileResponse{
        return SnippetFromFileResponse(input.title,input.description,userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByUserId(userId)
    }


}