package org.gudelker.snippet.service.modules.testsnippet

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.gudelker.snippet.service.modules.snippets.Snippet
import java.util.UUID

@Entity
class TestSnippet {
    @Id
    @Column(updatable = false, nullable = false)
    var id: UUID? = null

    @ElementCollection
    @Column(name = "input")
    var input: List<String>? = null

    @ElementCollection
    @Column(name = "expected_output")
    var expectedOutput: List<String>? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    lateinit var snippet: Snippet
}
