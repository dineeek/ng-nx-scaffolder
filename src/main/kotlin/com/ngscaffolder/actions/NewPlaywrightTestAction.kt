package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.PlaywrightTestDialog
import com.ngscaffolder.generators.PlaywrightTestGenerator

class NewPlaywrightTestAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = PlaywrightTestDialog()
        if (!dialog.showAndGet()) return

        val featureName = dialog.featureName.trim()
        val testCaseName = dialog.testCaseName.trim()
        if (featureName.isEmpty() || testCaseName.isEmpty()) return

        runWriteAction {
            PlaywrightTestGenerator(project).generate(directory, featureName, testCaseName)
        }
    }
}
