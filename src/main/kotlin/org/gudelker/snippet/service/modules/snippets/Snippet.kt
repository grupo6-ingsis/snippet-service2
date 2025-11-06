// src/main/kotlin/org/gudelker/snippet/service/modules/snippets/Snippet.kt
package org.gudelker.snippet.service.modules.snippets

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import org.gudelker.snippet.service.modules.lint_result.LintResult
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
    var description: String = "",
    @Column(nullable = false)
    var language: String = "",
    @Column
    var snippetVersion: Version,
    @Column(nullable = false)
    var created: OffsetDateTime = OffsetDateTime.now(),
    @Column(nullable = false)
    var updated: OffsetDateTime = OffsetDateTime.now(),

    @OneToOne(mappedBy = "snippet", cascade = [CascadeType.ALL], orphanRemoval = true)
    var lintResult: LintResult? = null

)
