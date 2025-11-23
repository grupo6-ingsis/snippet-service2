package org.gudelker.snippet.service.modules.interpret

import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.EngineApiClient
import org.springframework.stereotype.Service

@Service
class InterpretService(private val engineApiClient: EngineApiClient, private val authApiClient: AuthApiClient) {
    fun interpretSnippet(
        request: InterpretSnippetRequest,
        snippetId: String,
        userId: String,
    ): InterpretSnippetResponse {
        val permission = authApiClient.hasPermission(snippetId, userId)
        println("Permission: $permission")
        if (permission == null) {
            throw IllegalAccessException("User is not authorized to run test snippets for this snippet")
        }
        return engineApiClient.interpretSnippet(request)
    }
}
