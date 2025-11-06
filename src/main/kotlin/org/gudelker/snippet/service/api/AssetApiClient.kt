package org.gudelker.snippet.service.api

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class AssetApiClient(
    private val restClient: RestClient,
) {
    private val baseUrl = "http://asset-service:8080/v1/asset"

    fun generatePresignedUrl(
        container: String,
        key: String,
    ): String {
        // Opción 1: Si asset-service soporta presigned URLs (ideal)
        // return restClient.post()
        //     .uri("$baseUrl/presigned-url")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .body(mapOf("container" to container, "key" to key))
        //     .retrieve()
        //     .body(String::class.java)
        //     ?: throw RuntimeException("Error generating presigned URL")

        // Opción 2: URL directa del asset-service (más simple pero requiere auth)
        return "$baseUrl/$container/$key"
    }

    fun createAsset(
        container: String,
        key: String,
        content: String,
    ) {
        restClient.put()
            .uri("$baseUrl/{container}/{key}", container, key)
            .contentType(MediaType.TEXT_PLAIN)
            .body(content)
            .retrieve()
            .toBodilessEntity()
    }

    fun getAsset(
        container: String,
        key: String,
    ): String {
        return restClient.get()
            .uri("$baseUrl/{container}/{key}", container, key)
            .retrieve()
            .body(String::class.java)
            ?: throw RuntimeException("Error fetching asset")
    }

    fun deleteAsset(
        container: String,
        key: String,
    ) {
        try {
            restClient.delete()
                .uri("$baseUrl/{container}/{key}", container, key)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            // Log pero no fallar si ya fue borrado
            println("Warning: Could not delete asset $container/$key: ${e.message}")
        }
    }
}
