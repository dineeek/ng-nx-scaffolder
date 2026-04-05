package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.ngscaffolder.settings.PluginSettings
import javax.swing.JComponent

class NxLibraryDialog : DialogWrapper(true) {

    private val settings = PluginSettings.getInstance().state

    var domain: String = settings.nxDefaultDomain
    var libType: String = settings.nxDefaultType
    var libName: String = ""

    init {
        title = "New Nx Library"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Domain:") {
            textField()
                .bindText(::domain)
                .focused()
                .comment("e.g. sales, ffu, shared")
        }
        row("Type:") {
            textField()
                .bindText(::libType)
                .comment("feature, ui, data-access, util, model")
        }
        row("Name:") {
            textField()
                .bindText(::libName)
                .comment("e.g. offers → libs/{domain}/feature-offers/")
        }
    }
}
