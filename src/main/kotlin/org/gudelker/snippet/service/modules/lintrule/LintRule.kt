package org.gudelker.snippet.service.modules.lintrule

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class LintRule {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    var id: UUID? = null
    @Column(nullable = false, unique = true)
    var name: String = ""
    @Column(nullable = false)
    var description: String = ""
}