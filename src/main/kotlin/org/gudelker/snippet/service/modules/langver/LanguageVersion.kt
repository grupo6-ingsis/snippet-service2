package org.gudelker.snippet.service.modules.langver

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.gudelker.snippet.service.modules.language.Language
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class LanguageVersion {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    val id: UUID? = null

    @Column(updatable = false, nullable = false)
    lateinit var version: String

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    lateinit var language: Language
}
