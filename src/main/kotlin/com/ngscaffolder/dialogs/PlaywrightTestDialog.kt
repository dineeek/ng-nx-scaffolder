package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class PlaywrightTestDialog : DialogWrapper(true) {

    var featureName: String = ""
    var testCaseName: String = ""

    init {
        title = "New Playwright E2E Test"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Feature name:") {
            textField()
                .bindText(::featureName)
                .focused()
                .comment("e.g. offer-contract-flow → feature-offer-contract-flow/")
        }
        row("Test case name:") {
            textField()
                .bindText(::testCaseName)
                .comment("e.g. create-offer → create-offer.ts")
        }
    }
}
