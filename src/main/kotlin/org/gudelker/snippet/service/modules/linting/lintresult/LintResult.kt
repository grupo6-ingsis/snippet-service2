package org.gudelker.snippet.service.modules.linting.lintresult

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.types.ComplianceType
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
class LintResult {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    var id: UUID? = null

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snippet_id", nullable = false, unique = true)
    var snippet: Snippet? = null

    @Column(nullable = false)
    var complianceType: ComplianceType = ComplianceType.PENDING

    @Column(nullable = false)
    var lintedAt: LocalDateTime = LocalDateTime.now()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lint_result_errors", joinColumns = [JoinColumn(name = "lint_result_id")])
    var errors: MutableList<LintError> = mutableListOf()
}

@Embeddable
data class LintError(
    @Column(nullable = false)
    var message: String = "",
    @Column(nullable = false)
    var line: Int = 0,
    @Column(nullable = false, name = "column_number")
    var columnNumber: Int = 0,
)
