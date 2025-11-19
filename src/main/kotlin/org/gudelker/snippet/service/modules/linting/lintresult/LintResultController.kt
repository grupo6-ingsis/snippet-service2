package org.gudelker.snippet.service.modules.linting.lintresult

import org.gudelker.snippet.service.redis.dto.SnippetIdWithLintResultsDto
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lintresult")
class LintResultController(
    private val lintResultService: LintResultService,
) {
    @PutMapping("/all")
    fun saveLintResults(
        @RequestBody result: SnippetIdWithLintResultsDto,
    ): SnippetIdWithLintResultsDto {
        return lintResultService.createOrUpdateLintResult(result.snippetId, result.results)
    }
}
