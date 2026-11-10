package io.github.lechaosx.difftastic

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import java.nio.file.Files
import java.util.concurrent.TimeUnit

object DifftasticRunner {
    private val log = Logger.getInstance(DifftasticRunner::class.java)
    private const val TIMEOUT_SECONDS = 30L

    fun run(
        text1: CharSequence,
        text2: CharSequence,
        extension: String,
        difftasticPath: String,
        indicator: ProgressIndicator,
    ): DifftasticResult? {
        val lhs = Files.createTempFile("dft-lhs-", ".$extension")
        val rhs = Files.createTempFile("dft-rhs-", ".$extension")
        try {
            Files.writeString(lhs, text1)
            Files.writeString(rhs, text2)
            indicator.checkCanceled()

            val process = ProcessBuilder(difftasticPath, lhs.toString(), rhs.toString())
                .also {
                    it.environment()["DFT_UNSTABLE"] = "yes"
                    it.environment()["DFT_DISPLAY"] = "json"
                }
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            indicator.checkCanceled()

            if (!finished) {
                process.destroyForcibly()
                log.warn("Difftastic: process timed out after ${TIMEOUT_SECONDS}s")
                return null
            }
            if (process.exitValue() != 0) {
                log.warn("Difftastic: process exited with ${process.exitValue()}, output: $output")
                return null
            }

            val result = DifftasticParser.parse(output)
            if (result == null) {
                log.warn("Difftastic: failed to parse JSON output: $output")
                return null
            }

            log.info("Difftastic: status=${result.status} language=${result.language}")
            return if (result.status == "changed") result else null
        } finally {
            Files.deleteIfExists(lhs)
            Files.deleteIfExists(rhs)
        }
    }
}
