package org.gudelker.snippet.service.modules.snippets.dto.types

enum class AccessType {
    OWNER,
    SHARED,
    ALL,
}

enum class DirectionType {
    ASC,
    DESC,
}

enum class SortByType {
    NAME,
    LANGUAGE,
    PASSED_LINT,
}

enum class ComplianceType {
    COMPLIANT,
    NON_COMPLIANT,
    FAILED,
    PENDING,
}
