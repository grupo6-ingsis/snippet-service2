package org.gudelker.snippet.service.modules.lint_config

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.gudelker.snippet.service.modules.lint_rule.LintRule
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class LintConfig {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    var id: UUID? = null
    @Column(nullable = false)
    var userId: String = ""
    @ManyToOne
    @JoinColumn(name = "lint_rule_id", nullable = false)
    var lintRule: LintRule? = null
}