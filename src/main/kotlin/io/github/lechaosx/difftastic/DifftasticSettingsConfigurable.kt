package io.github.lechaosx.difftastic

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import java.io.File

class DifftasticSettingsConfigurable : BoundConfigurable("Difftastic") {
    private val settings get() = DifftasticSettings.instance

    override fun createPanel(): DialogPanel = panel {
        row {
            label("Difftastic binary:")
        }
        row {
            textFieldWithBrowseButton(
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                    .withTitle("Select difft Executable")
            )
                .bindText(settings::difftasticPath)
                .align(AlignX.FILL)
                .applyToComponent {
                    (textField as? JBTextField)?.emptyText?.text = "difft"
                }
                .validationOnInput { if (!available(it.text)) ValidationInfo("difft not available") else null }
                .validationOnApply { if (!available(it.text)) ValidationInfo("difft not available") else null }
                .comment("Path, alias, or command. Leave empty to use difft from \$PATH.")
        }
    }.also { it.registerValidators(disposable!!) }

    private fun available(text: String): Boolean {
        val cmd = text.ifBlank { "difft" }
        if (cmd.contains('/') || cmd.contains('\\')) return File(cmd).canExecute()
        return (System.getenv("PATH") ?: "").split(File.pathSeparator)
            .any { File(it, cmd).canExecute() }
    }
}
