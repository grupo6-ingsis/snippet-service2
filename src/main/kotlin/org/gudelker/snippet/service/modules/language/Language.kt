package org.gudelker.snippet.service.modules.language

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.gudelker.snippet.service.modules.langver.LanguageVersion
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class Language {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    val id: UUID? = null

    @Column
    var name: String = ""

    @Column
    var extension: String = ""

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "language", cascade = [CascadeType.ALL], orphanRemoval = true)
    var languageVersions: MutableList<LanguageVersion> = mutableListOf()
}
