@file:Suppress("UnstableApiUsage")

package io.github.lechaosx.difftastic

import com.intellij.diff.DiffContext
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.ContentDiffRequest
import com.intellij.diff.requests.DiffRequest
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager

class DifftasticDiffTool : FrameDiffTool {
    private val log = Logger.getInstance(DifftasticDiffTool::class.java)
    @Volatile private var cachedPath: String? = null

    override fun getName(): String = "Difftastic"

    override fun canShow(context: DiffContext, request: DiffRequest): Boolean {
        if (request !is ContentDiffRequest || request.contents.size != 2) return false
        if (!request.contents.all { it is DocumentContent }) return false
        cachedPath = DifftasticSettings.findBinary()
        if (cachedPath == null) log.warn("difft binary not found; Difftastic diff tool will not be shown")
        return cachedPath != null
    }

    override fun createComponent(context: DiffContext, request: DiffRequest): FrameDiffTool.DiffViewer {
        request as ContentDiffRequest
        return DifftasticViewer.create(
            context,
            request,
            difftasticPath = cachedPath ?: DifftasticSettings.findBinary() ?: error("difft binary not found"),
            extension = guessExtension(request),
        )
    }

    private fun guessExtension(request: ContentDiffRequest): String {
        for (content in request.contents) {
            if (content !is DocumentContent) continue
            val ext = FileDocumentManager.getInstance().getFile(content.document)?.extension
            if (!ext.isNullOrBlank()) return ext
        }
        return "txt"
    }
}
