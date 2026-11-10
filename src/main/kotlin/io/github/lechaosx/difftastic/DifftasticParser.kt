package io.github.lechaosx.difftastic

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object DifftasticParser {
    private val gson = Gson()

    fun parse(json: String): DifftasticResult? = try {
        gson.fromJson(json.trim(), DifftasticResult::class.java)
    } catch (_: JsonSyntaxException) {
        null
    }
}
