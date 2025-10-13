package org.gudelker.snippet.service.snippets

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Snippet(
    @Id
    @GeneratedValue(generator = "uuid2")
    @org.hibernate.annotations.GenericGenerator(name = "uuid2", strategy = "uuid2")
    var id: String? = null
)
