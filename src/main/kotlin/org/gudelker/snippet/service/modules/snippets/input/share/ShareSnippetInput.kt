package org.gudelker.snippet.service.modules.snippets.input.share

import java.util.UUID

data class ShareSnippetInput(
    val sharedId: String,
    val snippetId: UUID,
)
