package org.gudelker.snippet.service.modules.snippets

import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.snippets.dto.ParseSnippetRequest
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.create.FinalizeSnippetRequest
import org.gudelker.snippet.service.modules.snippets.dto.create.InitiateSnippetUploadResponse
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromEditorResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.create.InitiateSnippetUploadInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SnippetService(
    private val snippetRepository: SnippetRepository,
    private val authApiClient: AuthApiClient,
    private val assetApiClient: AssetApiClient,
) {
    fun initiateSnippetUpload(
        input: InitiateSnippetUploadInput,
        jwt: Jwt,
    ): InitiateSnippetUploadResponse {
        val snippetId = UUID.randomUUID()

        // Validar extensión del archivo (opcional)
        val allowedExtensions = listOf(".ps", ".kt", ".java", ".py")
        val extension = input.filename.substringAfterLast(".")
        if (!allowedExtensions.contains(".$extension")) {
            throw IllegalArgumentException("Extension .$extension not allowed")
        }

        // Obtener presigned URL del asset-service para bucket TEMPORAL
        val presignedUrlResponse =
            assetApiClient.generatePresignedUrl(
                container = "snippets-temp",
                key = snippetId.toString(),
                expiresInMinutes = 5,
            )

        return InitiateSnippetUploadResponse(
            snippetId = snippetId,
            uploadUrl = presignedUrlResponse.uploadUrl,
            expiresIn = 300,
        )
    }

    /**
     * PASO 2: Validación transaccional + persistencia
     * - Descarga del bucket temporal
     * - Parsea con engine
     * - Si OK: guarda en DB + mueve a bucket final + crea permisos
     * - Si FAIL: rollback completo (borra temporal, no guarda en DB)
     */
    @Transactional(rollbackFor = [Exception::class])
    fun finalizeSnippetUpload(
        request: FinalizeSnippetRequest,
        jwt: Jwt,
    ): Snippet {
        val snippetId = UUID.fromString(request.snippetId)
        val userId = jwt.subject

        // 1. Descargar contenido del bucket temporal
        val content =
            try {
                assetApiClient.getAsset("snippets-temp", snippetId.toString())
            } catch (e: Exception) {
                throw IllegalStateException(
                    "Snippet no encontrado en el bucket temporal. " +
                        "Asegúrate de haber subido el archivo primero.",
                    e,
                )
            }

        // 2. Validar con el engine (parse)
        val parseRequest =
            ParseSnippetRequest(
                snippetContent = content,
                version = request.version,
            )

        val parseResult =
            try {
                authApiClient.parseSnippet(parseRequest)
            } catch (e: Exception) {
                // Si falla la comunicación con el engine, limpiar y abortar
                assetApiClient.deleteAsset("snippets-temp", snippetId.toString())
                throw IllegalStateException(
                    "Error al comunicarse con el engine: ${e.message}",
                    e,
                )
            }

        // 3. Si el parseo falla, rollback
        if (parseResult == ResultType.FAILURE) {
            assetApiClient.deleteAsset("snippets-temp", snippetId.toString())
            throw IllegalArgumentException(
                "El snippet tiene errores de sintaxis y no puede ser guardado. " +
                    "Por favor, corrige los errores e intenta nuevamente.",
            )
        }

        // 4. Mover a bucket definitivo (promoción)
        try {
            // Copiar a bucket final
            assetApiClient.createAsset("snippets", snippetId.toString(), content)

            // Borrar del temporal (limpieza)
            assetApiClient.deleteAsset("snippets-temp", snippetId.toString())
        } catch (e: Exception) {
            // Si falla la copia, limpiar temporal y abortar
            assetApiClient.deleteAsset("snippets-temp", snippetId.toString())
            throw IllegalStateException(
                "Error al mover el snippet al bucket definitivo: ${e.message}",
                e,
            )
        }

        // 5. Guardar metadata en DB
        val snippet =
            Snippet(
                id = snippetId,
                ownerId = userId,
                title = request.title,
                description = request.description ?: "",
                language = request.language,
                snippetVersion = request.version,
                created = OffsetDateTime.now(),
                updated = OffsetDateTime.now(),
            )

        val savedSnippet =
            try {
                snippetRepository.save(snippet)
            } catch (e: Exception) {
                // Si falla guardar en DB, limpiar bucket definitivo
                assetApiClient.deleteAsset("snippets", snippetId.toString())
                throw IllegalStateException(
                    "Error al guardar el snippet en la base de datos: ${e.message}",
                    e,
                )
            }

        // 6. Crear permisos en authorization-service
        val authorizeRequest =
            AuthorizeRequestDto(
                userId = userId,
                permission = PermissionType.WRITE,
            )

        try {
            authApiClient.authorizeSnippet(snippetId, authorizeRequest)
        } catch (e: Exception) {
            // Si falla autorización, limpiar asset (DB ya hizo rollback por @Transactional)
            assetApiClient.deleteAsset("snippets", snippetId.toString())
            throw IllegalStateException(
                "Error al autorizar permisos: ${e.message}",
                e,
            )
        }

        return savedSnippet
    }

    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippetFromFile(
        input: CreateSnippetFromFileInput,
        jwt: Jwt,
    ): SnippetFromFileResponse {
        val snippetId = UUID.randomUUID()
        val userId =
            jwt.claims["sub"] as? String
                ?: throw IllegalArgumentException("JWT missing 'sub' claim")
        val request = createAuthorizeRequestDto(userId, PermissionType.WRITE)
        try {
            authApiClient.authorizeSnippet(snippetId, request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val content = input.content
        val container = "snippets"
        assetApiClient.createAsset(container, snippetId.toString(), content)

        val snippet = createSnippet(snippetId, userId, input)
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input, userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByOwnerId(userId)
    }

    fun updateSnippetFromFile(input: UpdateSnippetFromFileInput): UpdateSnippetFromFileResponse {
        if (input.title == null && input.content == null && input.language == null) {
            throw IllegalArgumentException("At least one attribute (title, content, language) must be provided for update.")
        }
        val snippet =
            snippetRepository.findById(input.snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }

        val authorization = authApiClient.authorizeUpdateSnippet(input.snippetId)
        if (!authorization) {
            throw RuntimeException("Authorization failed")
        }

        input.title?.let { snippet.title = it }
        input.language?.let { snippet.language = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        if (input.content != null) {
            return UpdateSnippetFromFileResponse(
                snippetId = snippet.id.toString(),
                title = snippet.title,
                content = input.content,
                language = snippet.language,
                updated = snippet.updated,
            )
        }
        return UpdateSnippetFromFileResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            content = assetApiClient.getAsset("snippets", snippet.id.toString()),
            language = snippet.language,
            updated = snippet.updated,
        )
    }

    fun getSnippetById(snippetId: String): Snippet {
        return snippetRepository.findById(UUID.fromString(snippetId))
            .orElseThrow { RuntimeException("Snippet not found") }
    }

    @Transactional
    fun createSnippetFromEditor(
        input: CreateSnippetFromEditor,
        jwt: Jwt,
    ): Snippet {
        val userId = jwt.subject
        val authorizeRequest = createAuthorizeRequestDto(userId, PermissionType.WRITE)
//        try {
//            val parseRequest =
//                ParseSnippetRequest(
//                    snippetContent = input.content,
//                    version = input.version,
//                )
//            val result = authApiClient.parseSnippet(parseRequest)
//            if (result == ResultType.FAILURE) {
//                throw IllegalArgumentException("Snippet parsing failed")
//            }
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//            throw ex
//        }

        val snippet =
            Snippet(
                ownerId = userId,
                title = input.title,
                language = input.language,
                description = input.description,
                snippetVersion = input.version,
                created = OffsetDateTime.now(),
                updated = OffsetDateTime.now(),
            )
        val saved = snippetRepository.save(snippet)

        try {
            if (saved.id == null) {
                throw RuntimeException("Failed to save snippet")
            }
            authApiClient.authorizeSnippet(saved.id!!, authorizeRequest)
        } catch (ex: Exception) {
            throw RuntimeException("Authorization failed", ex)
        }
        try {
            assetApiClient.createAsset("snippets", saved.id.toString(), input.content)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to save content", ex)
        }
        return snippet
    }

    fun updateSnippetFromEditor(
        input: UpdateSnippetFromEditorInput,
        jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        if (input.title == null && input.content == null && input.language == null && input.description == null && input.version == null) {
            throw IllegalArgumentException(
                "At least one attribute (title, content, language, description, version) must be provided for update.",
            )
        }
        val snippetId = UUID.fromString(input.snippetId)
        val snippet =
            snippetRepository.findById(snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }

        val userId = jwt.subject
        try {
            val isAuthorized = authApiClient.isUserAuthorizedToWriteSnippet(snippetId.toString(), userId)
            if (!isAuthorized) {
                throw RuntimeException("User does not have WRITE permission for this snippet")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }

        if (input.content != null && input.version != null) {
            val parseRequest =
                ParseSnippetRequest(
                    snippetContent = input.content,
                    version = input.version,
                )
            val parseResult = authApiClient.parseSnippet(parseRequest)
            if (parseResult == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        }

        input.title?.let { snippet.title = it }
        input.description?.let { snippet.description = it }
        input.language?.let { snippet.language = it }
        input.version?.let { snippet.snippetVersion = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        return UpdateSnippetFromEditorResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            description = snippet.description,
            content = input.content,
            language = snippet.language,
            version = snippet.snippetVersion.toString(),
            updated = snippet.updated,
        )
    }

    private fun createAuthorizeRequestDto(
        userId: String,
        permission: PermissionType,
    ): AuthorizeRequestDto {
        return AuthorizeRequestDto(
            userId = userId,
            permission = permission,
        )
    }

    private fun createSnippet(
        id: UUID,
        ownerId: String,
        input: CreateSnippetFromFileInput,
    ): Snippet {
        return Snippet(
            id = id,
            ownerId = ownerId,
            title = input.title,
            language = input.language,
            snippetVersion = input.version,
            created = OffsetDateTime.now(),
            updated = OffsetDateTime.now(),
        )
    }

    private fun createSnippetFromFileResponse(
        input: CreateSnippetFromFileInput,
        userId: String,
    ): SnippetFromFileResponse {
        return SnippetFromFileResponse(input.title, input.content, userId)
    }
}
