package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.ngscaffolder.settings.PluginSettings
import javax.swing.JComponent

class SimpleLibDialog(
    dialogTitle: String,
    private val nameComment: String,
    private val showPrefix: Boolean = false,
) : DialogWrapper(true) {

    private val settings = PluginSettings.getInstance().state

    var libName: String = ""
    var prefix: String = settings.selectorPrefix

    init {
        title = dialogTitle
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Name:") {
            textField()
                .bindText(::libName)
                .focused()
                .comment(nameComment)
        }
        if (showPrefix) {
            row("Selector prefix:") {
                textField()
                    .bindText(::prefix)
            }
        }
    }
}
