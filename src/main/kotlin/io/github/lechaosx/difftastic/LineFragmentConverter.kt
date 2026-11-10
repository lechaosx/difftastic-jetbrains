package io.github.lechaosx.difftastic

import com.intellij.diff.fragments.DiffFragmentImpl
import com.intellij.diff.fragments.LineFragment
import com.intellij.diff.fragments.LineFragmentImpl

object LineFragmentConverter {

    fun convert(
        text1: CharSequence,
        text2: CharSequence,
        result: DifftasticResult,
        focused: Set<String>,
    ): List<LineFragment> {
        if (result.chunks.isNullOrEmpty()) return emptyList()
        val lo1 = buildLineOffsets(text1)
        val lo2 = buildLineOffsets(text2)

        // aligned_lines: exact anchor on the opposite side for every pure insertion/deletion line.
        // rhsAnchor[R] = lhs line just after the last paired line before rhs-only line R
        // lhsAnchor[L] = rhs line just after the last paired line before lhs-only line L
        val rhsAnchor = mutableMapOf<Int, Int>()
        val lhsAnchor = mutableMapOf<Int, Int>()
        result.aligned_lines?.let { lines ->
            var lastLhs = 0; var lastRhs = 0
            for (entry in lines) {
                val lhs = entry.getOrNull(0); val rhs = entry.getOrNull(1)
                if (lhs == null && rhs != null) rhsAnchor[rhs] = lastLhs
                if (rhs == null && lhs != null) lhsAnchor[lhs] = lastRhs
                if (lhs != null) lastLhs = lhs + 1
                if (rhs != null) lastRhs = rhs + 1
            }
        }

        return buildList {
            // curLine1/curLine2 are fallbacks only — used when aligned_lines is absent.
            var curLine1 = 0
            var curLine2 = 0

            for (chunk in result.chunks) {
                val lhsNums = chunk.mapNotNull { it.lhs?.line_number }
                val rhsNums = chunk.mapNotNull { it.rhs?.line_number }
                if (lhsNums.isEmpty() && rhsNums.isEmpty()) continue

                val s1 = lhsNums.minOrNull() ?: rhsAnchor.getOrDefault(rhsNums.min(), curLine1)
                val e1 = lhsNums.maxOrNull()?.plus(1) ?: s1
                val s2 = rhsNums.minOrNull() ?: lhsAnchor.getOrDefault(lhsNums.min(), curLine2)
                val e2 = rhsNums.maxOrNull()?.plus(1) ?: s2

                val so1 = lo1.getOrElse(s1) { lo1.last() }
                val eo1 = lo1.getOrElse(e1) { lo1.last() }
                val so2 = lo2.getOrElse(s2) { lo2.last() }
                val eo2 = lo2.getOrElse(e2) { lo2.last() }

                val inner = buildInner(chunk, lo1, lo2, so1, so2, s2, rhsAnchor, lhsAnchor, focused)
                if (inner.isEmpty()) continue
                add(LineFragmentImpl(s1, e1, s2, e2, so1, eo1, so2, eo2, inner))

                curLine1 = maxOf(curLine1, e1)
                curLine2 = maxOf(curLine2, e2)
            }
        }
    }

    private fun buildInner(
        chunk: List<DifftasticLine>,
        lo1: IntArray, lo2: IntArray,
        fragmentStart1: Int, fragmentStart2: Int,
        chunkS2: Int,
        rhsAnchor: Map<Int, Int>, lhsAnchor: Map<Int, Int>,
        focused: Set<String>,
    ): List<DiffFragmentImpl> = buildList {
        for (line in chunk) {
            val lhs = line.lhs; val rhs = line.rhs

            val lhsBase = lhs?.let { lo1.getOrElse(it.line_number) { 0 } - fragmentStart1 } ?: 0
            val rhsBase = when {
                rhs != null -> lo2.getOrElse(rhs.line_number) { 0 } - fragmentStart2
                lhs != null -> lo2.getOrElse(lhsAnchor.getOrDefault(lhs.line_number, chunkS2)) { lo2.last() } - fragmentStart2
                else        -> 0
            }

            when {
                lhs != null && rhs != null -> {
                    for (c in lhs.changes) if (c.start < c.end && c.isVisible(focused))
                        add(DiffFragmentImpl(lhsBase + c.start, lhsBase + c.end, rhsBase, rhsBase))
                    for (c in rhs.changes) if (c.start < c.end && c.isVisible(focused))
                        add(DiffFragmentImpl(lhsBase, lhsBase, rhsBase + c.start, rhsBase + c.end))
                }
                lhs != null -> emitLineOrTokens(lhs.changes.filter { it.isVisible(focused) },
                    lhsBase, rhsBase, isLhs = true)
                rhs != null -> emitLineOrTokens(rhs.changes.filter { it.isVisible(focused) },
                    rhsBase, lhsBase, isLhs = false)
            }
        }
    }

    private fun BuilderScope.emitLineOrTokens(
        changes: List<DifftasticChange>, base: Int, anchor: Int, isLhs: Boolean,
    ) {
        for (c in changes) if (c.start < c.end)
            add(if (isLhs) DiffFragmentImpl(base + c.start, base + c.end, anchor, anchor)
                else        DiffFragmentImpl(anchor, anchor, base + c.start, base + c.end))
    }

    // Sentinel at end equals text.length so getOrElse(endLine) is well-defined for the
    // line immediately past the last newline.
    private fun buildLineOffsets(text: CharSequence): IntArray {
        val newlines = text.count { it == '\n' }
        val offsets = IntArray(newlines + 2)
        var n = 1
        for (i in text.indices) if (text[i] == '\n') offsets[n++] = i + 1
        offsets[n] = text.length
        return offsets
    }
}

// A change is hidden when the user's IgnorePolicy says to ignore whitespace and
// the token is pure whitespace (e.g. indentation changes difftastic surfaces as "normal" tokens).
// Empty focused set = no token highlights. tree_sitter_error always shown when anything is focused.
private fun DifftasticChange.isVisible(focused: Set<String>): Boolean =
    focused.isNotEmpty() && (highlight == "tree_sitter_error" || highlight in focused)

private typealias BuilderScope = MutableList<DiffFragmentImpl>
