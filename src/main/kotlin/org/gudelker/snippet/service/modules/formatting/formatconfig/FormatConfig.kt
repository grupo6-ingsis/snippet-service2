package org.gudelker.snippet.service.modules.formatting.formatconfig

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRule
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
class FormatConfig {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    var id: UUID? = null

    @Column(nullable = false)
    var userId: String = ""

    @ManyToOne
    @JoinColumn(name = "format_rule_id", nullable = false)
    var formatRule: FormatRule? = null

    @Column
    var ruleValue: Int? = null
}
