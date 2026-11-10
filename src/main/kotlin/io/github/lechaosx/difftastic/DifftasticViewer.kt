@file:Suppress("UnstableApiUsage")

package io.github.lechaosx.difftastic

import com.intellij.diff.DiffContext
import com.intellij.diff.requests.ContentDiffRequest
import com.intellij.diff.tools.simple.SimpleDiffViewer
import com.intellij.diff.util.DiffUserDataKeysEx
import com.intellij.openapi.util.Key
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbAwareToggleAction

class DifftasticViewer private constructor(
    context: DiffContext,
    private val contentRequest: ContentDiffRequest,
    private val state: State,
) : SimpleDiffViewer(context, contentRequest) {

    override fun onDispose() {
        super.onDispose()
        contentRequest.putUserData(DiffUserDataKeysEx.CUSTOM_DIFF_COMPUTER, null)
    }

    override fun createToolbarActions(): MutableList<AnAction> =
        super.createToolbarActions()
            .filterNotTo(mutableListOf()) { it is ComboBoxAction }
            .also { actions ->
                State.TOGGLEABLE.forEach { (highlight, label, icon) ->
                    actions.add(FocusAction(label, icon, highlight))
                }
            }

    private inner class FocusAction(
        label: String,
        icon: javax.swing.Icon,
        private val highlight: String,
    ) : DumbAwareToggleAction(label, "Include $label changes (deselect all = no highlights)", icon) {
        override fun isSelected(e: AnActionEvent) = highlight in state.focused
        override fun setSelected(e: AnActionEvent, selected: Boolean) {
            if (selected) state.focused.add(highlight) else state.focused.remove(highlight)
            scheduleRediff()
        }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class State {
        val focused = java.util.concurrent.CopyOnWriteArraySet(TOGGLEABLE.map { it.first }.toSet())

        companion object {
            val TOGGLEABLE = listOf(
                Triple("normal",    "Identifiers", AllIcons.Nodes.Variable),
                Triple("keyword",   "Keywords",    AllIcons.Nodes.AbstractMethod),
                Triple("type",      "Types",       AllIcons.Nodes.Type),
                Triple("string",    "Strings",     AllIcons.Nodes.Static),
                Triple("comment",   "Comments",    AllIcons.Debugger.Console),
                Triple("delimiter", "Delimiters",  AllIcons.Actions.GroupByPrefix),
            )
        }
    }

    companion object {
        private val log = Logger.getInstance(DifftasticViewer::class.java)

        private data class CacheKey(val h1: Int, val h2: Int, val extension: String)
        private val CACHE_KEY = Key.create<HashMap<CacheKey, DifftasticResult>>("difftastic.cache")

        fun create(
            context: DiffContext,
            request: ContentDiffRequest,
            difftasticPath: String,
            extension: String,
        ): DifftasticViewer {
            val state = State()
            // Cache lives on the request so it survives tool switches within the same tab.
            val cache = request.getUserData(CACHE_KEY) ?: HashMap<CacheKey, DifftasticResult>()
                .also { request.putUserData(CACHE_KEY, it) }
            request.putUserData(
                DiffUserDataKeysEx.CUSTOM_DIFF_COMPUTER,
                DiffUserDataKeysEx.DiffComputer { text1, text2, _, _, indicator ->
                    try {
                        val key = CacheKey(text1.toString().hashCode(), text2.toString().hashCode(), extension)
                        val result = cache[key]
                            ?: DifftasticRunner.run(text1, text2, extension, difftasticPath, indicator)
                                ?.also { cache[key] = it }
                            ?: return@DiffComputer emptyList()
                        LineFragmentConverter.convert(text1, text2, result, state.focused)
                    } catch (e: ProcessCanceledException) {
                        throw e
                    } catch (e: Exception) {
                        log.warn("Difftastic diff failed", e)
                        emptyList()
                    }
                }
            )
            return DifftasticViewer(context, request, state)
        }
    }
}
