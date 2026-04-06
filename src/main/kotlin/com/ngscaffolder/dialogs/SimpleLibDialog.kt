package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.ngscaffolder.settings.PluginSettings
import javax.swing.JComponent
import javax.swing.JTextField

class SimpleLibDialog(
    dialogTitle: String,
    private val nameComment: String,
    private val showPrefix: Boolean = false,
) : DialogWrapper(true) {

    private val settings = PluginSettings.getInstance().state

    var libName: String = ""
    var prefix: String = settings.selectorPrefix

    private lateinit var nameField: JTextField

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
                .also { nameField = it.component }
        }
        if (showPrefix) {
            row("Selector prefix:") {
                textField()
                    .bindText(::prefix)
            }
        }
    }

    override fun doValidate(): ValidationInfo? {
        val name = nameField.text.trim()
        if (name.isEmpty()) {
            return ValidationInfo("Name is required", nameField)
        }
        if (!name.matches(Regex("^[a-z][a-z0-9-]*$"))) {
            return ValidationInfo("Name must be kebab-case (e.g. my-service)", nameField)
        }
        return null
    }
}
