package org.gudelker.snippet.service.snippets

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class Snippet(
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,
    @Column(nullable = false)
    var userId: String,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var content: String,
)
