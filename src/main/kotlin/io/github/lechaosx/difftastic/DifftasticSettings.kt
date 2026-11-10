package io.github.lechaosx.difftastic

import com.intellij.openapi.components.*
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.io.IOException

@Service(Service.Level.APP)
@State(name = "DifftasticSettings", storages = [Storage("difftastic.xml")])
class DifftasticSettings : PersistentStateComponent<DifftasticSettings.State> {
    data class State(var difftasticPath: String = "")

    private var myState = State()

    var difftasticPath: String
        get() = myState.difftasticPath
        set(value) { myState.difftasticPath = value }

    override fun getState(): State = myState
    override fun loadState(state: State) { myState = state }

    companion object {
        private val log = Logger.getInstance(DifftasticSettings::class.java)

        val instance: DifftasticSettings get() = service()

        @Volatile private var cache: Pair<Long, String?>? = null
        private const val CACHE_TTL_MS = 30_000L

        fun findBinary(): String? {
            val now = System.currentTimeMillis()
            cache?.let { (ts, path) -> if (now - ts < CACHE_TTL_MS) return path }
            return synchronized(this) {
                cache?.let { (ts, path) -> if (now - ts < CACHE_TTL_MS) return@synchronized path }
                val result = resolve()
                cache = now to result
                result
            }
        }

        private fun resolve(): String? {
            val configured = instance.difftasticPath
            if (configured.isNotBlank()) {
                return if (File(configured).canExecute()) {
                    log.info("Difftastic: using configured binary at $configured")
                    configured
                } else {
                    log.warn("Difftastic: configured path '$configured' is not executable")
                    null
                }
            }
            return try {
                ProcessBuilder("difft", "--version").start().destroy()
                log.info("Difftastic: found 'difft' on PATH")
                "difft"
            } catch (_: IOException) {
                log.warn("Difftastic: 'difft' not found on PATH — plugin will not be active")
                null
            }
        }
    }
}
