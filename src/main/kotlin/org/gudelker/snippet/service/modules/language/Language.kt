package org.gudelker.snippet.service.modules.language

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@JsonIgnoreProperties(value = ["hibernateLazyInitializer", "handler"], ignoreUnknown = true)
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
}
