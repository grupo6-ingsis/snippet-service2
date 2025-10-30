// src/main/kotlin/org/gudelker/snippet/service/modules/snippets/Snippet.kt
package org.gudelker.snippet.service.modules.snippets

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.gudelker.snippet.service.modules.snippets.dto.Version
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
class Snippet(
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,
    @Column(nullable = false)
    var ownerId: String = "",
    @Column(nullable = false)
    var title: String = "",
    @Column(nullable = false)
    var content: String = "",
    @Column
    var description: String = "",
    @Column(nullable = false)
    var language: String = "",
    @Column
    var version: Version,
    @Column(nullable = false)
    var created: OffsetDateTime = OffsetDateTime.now(),
    @Column(nullable = false)
    var updated: OffsetDateTime = OffsetDateTime.now(),
)
