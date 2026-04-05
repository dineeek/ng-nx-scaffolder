package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class SignalStoreDialog : DialogWrapper(true) {

    var storeName: String = ""

    init {
        title = "New NgRx SignalStore"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Store name:") {
            textField()
                .bindText(::storeName)
                .focused()
                .comment("e.g. user-profile → user-profile.store.ts + user-profile.state.ts")
        }
    }
}
