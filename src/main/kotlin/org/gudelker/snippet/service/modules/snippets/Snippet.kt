// src/main/kotlin/org/gudelker/snippet/service/modules/snippets/Snippet.kt
package org.gudelker.snippet.service.modules.snippets

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.gudelker.snippet.service.modules.langver.LanguageVersion
import org.gudelker.snippet.service.modules.linting.lintresult.LintResult
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@JsonIgnoreProperties(value = ["hibernateLazyInitializer", "handler"], ignoreUnknown = true)
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
    var created: OffsetDateTime = OffsetDateTime.now(),
    @Column(nullable = false)
    var updated: OffsetDateTime = OffsetDateTime.now(),
    @OneToOne(mappedBy = "snippet", cascade = [CascadeType.ALL], orphanRemoval = true)
    var lintResult: LintResult? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var languageVersion: LanguageVersion,
)
