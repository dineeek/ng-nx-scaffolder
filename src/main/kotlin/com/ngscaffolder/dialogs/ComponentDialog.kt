package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class ComponentDialog : DialogWrapper(true) {

    var componentName: String = ""

    init {
        title = "New Angular Component"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Component name:") {
            textField()
                .bindText(::componentName)
                .focused()
                .comment("e.g. user-profile, UserProfile, or user profile")
        }
    }
}
