package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class DataAccessServiceDialog : DialogWrapper(true) {

    var serviceName: String = ""
    var entityName: String = ""

    init {
        title = "New Data-Access Service"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Service name:") {
            textField()
                .bindText(::serviceName)
                .focused()
                .comment("e.g. users → users.service.ts")
        }
        row("Entity name:") {
            textField()
                .bindText(::entityName)
                .comment("e.g. User → typed CRUD methods for User entity")
        }
    }
}
