package com.ngscaffolder.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JTextField

class PlaywrightTestDialog : DialogWrapper(true) {

    var featureName: String = ""
    var testCaseName: String = ""

    private lateinit var featureField: JTextField
    private lateinit var testCaseField: JTextField

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
                .also { featureField = it.component }
        }
        row("Test case name:") {
            textField()
                .bindText(::testCaseName)
                .comment("e.g. create-offer → create-offer.ts")
                .also { testCaseField = it.component }
        }
    }

    override fun doValidate(): ValidationInfo? {
        val feature = featureField.text.trim()
        if (feature.isEmpty()) {
            return ValidationInfo("Feature name is required", featureField)
        }
        if (!feature.matches(Regex("^[a-z][a-z0-9-]*$"))) {
            return ValidationInfo("Feature name must be kebab-case (e.g. offer-flow)", featureField)
        }

        val testCase = testCaseField.text.trim()
        if (testCase.isEmpty()) {
            return ValidationInfo("Test case name is required", testCaseField)
        }
        if (!testCase.matches(Regex("^[a-zA-Z][a-zA-Z0-9-]*$"))) {
            return ValidationInfo("Test case name must be kebab-case (e.g. create-offer)", testCaseField)
        }
        return null
    }
}
