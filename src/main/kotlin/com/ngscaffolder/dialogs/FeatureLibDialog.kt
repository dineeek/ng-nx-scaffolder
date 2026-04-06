package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import com.ngscaffolder.settings.PluginSettings
import javax.swing.JComponent
import javax.swing.JTextField

class FeatureLibDialog : DialogWrapper(true) {

    private val settings = PluginSettings.getInstance().state

    var libName: String = ""
    var prefix: String = settings.selectorPrefix
    var hasStore: Boolean = true
    var hasFacade: Boolean = false
    var hasForm: Boolean = false
    var hasRouting: Boolean = false
    var isDialog: Boolean = false

    private lateinit var nameField: JTextField

    init {
        title = "New Feature Library"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Name:") {
            textField()
                .bindText(::libName)
                .focused()
                .comment("e.g. user-profile, checkout, order-details")
                .also { nameField = it.component }
        }
        row("Selector prefix:") {
            textField()
                .bindText(::prefix)
        }
        separator()
        row {
            checkBox("Store (signalStore)")
                .bindSelected(::hasStore)
        }
        row {
            checkBox("Facade service")
                .bindSelected(::hasFacade)
        }
        row {
            checkBox("Form service")
                .bindSelected(::hasForm)
        }
        row {
            checkBox("Routing")
                .bindSelected(::hasRouting)
        }
        row {
            checkBox("Dialog component")
                .bindSelected(::isDialog)
                .comment("Wraps container in a dialog with MAT_DIALOG_DATA")
        }
    }

    override fun doValidate(): ValidationInfo? {
        val name = nameField.text.trim()
        if (name.isEmpty()) {
            return ValidationInfo("Name is required", nameField)
        }
        if (!name.matches(Regex("^[a-z][a-z0-9-]*$"))) {
            return ValidationInfo("Name must be kebab-case (e.g. my-feature)", nameField)
        }
        return null
    }
}
