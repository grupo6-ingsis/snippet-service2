package org.gudelker.snippet.service.modules.lintresult

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.gudelker.snippet.service.modules.lintrule.LintRule
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class LintResult {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    var id: UUID? = null
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snippet_id", nullable = false)
    var snippet: Snippet? = null
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lint_rule_id", nullable = false)
    var lintRule: LintRule? = null
    @Column(nullable = false)
    var passed: Boolean = false
    @Column(nullable = false)
    var line: Int = 0
    @Column(nullable = false)
    var column: Int = 0
    @Column
    var message: String? = null
}