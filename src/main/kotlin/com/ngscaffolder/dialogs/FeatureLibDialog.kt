package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import com.ngscaffolder.settings.PluginSettings
import javax.swing.JComponent

class FeatureLibDialog : DialogWrapper(true) {

    private val settings = PluginSettings.getInstance().state

    var libName: String = ""
    var domain: String = settings.nxDefaultDomain
    var prefix: String = settings.selectorPrefix
    var hasStore: Boolean = true
    var hasFacade: Boolean = false
    var hasForm: Boolean = false
    var hasRouting: Boolean = false
    var isDialog: Boolean = false

    init {
        title = "New Feature Library"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Name:") {
            textField()
                .bindText(::libName)
                .focused()
                .comment("e.g. offers, user-profile (without domain/type prefix)")
        }
        row("Domain:") {
            textField()
                .bindText(::domain)
                .comment("e.g. sales, ffu, hub")
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
}
