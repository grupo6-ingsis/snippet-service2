package org.gudelker.snippet.service.api

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class AssetApiClient(
    private val restClient: RestClient,
) {
    private val baseUrl = "http://asset-service:8080/v1/asset"

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
        restClient.delete()
            .uri("$baseUrl/{container}/{key}", container, key)
            .retrieve()
            .toBodilessEntity()
    }
}
