package io.github.lechaosx.difftastic

data class DifftasticResult(
    val status: String,
    val path: String?,
    val language: String?,
    val chunks: List<List<DifftasticLine>>?,
    val aligned_lines: List<List<Int?>>?,  // [[lhs_line, rhs_line], ...], null on either side = absent
)

data class DifftasticLine(
    val lhs: DifftasticSide?,
    val rhs: DifftasticSide?
)

data class DifftasticSide(
    val line_number: Int,
    val changes: List<DifftasticChange>
)

data class DifftasticChange(
    val start: Int,
    val end: Int,
    val content: String,
    val highlight: String,  // normal, keyword, string, type, comment, delimiter, tree_sitter_error
)
