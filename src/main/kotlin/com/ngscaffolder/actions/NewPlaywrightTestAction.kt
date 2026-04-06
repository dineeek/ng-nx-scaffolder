package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
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

        val file = try {
            runWriteAction {
                PlaywrightTestGenerator(project).generate(directory, featureName, testCaseName)
            }
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to generate Playwright test: ${ex.message}",
                "Generation Failed"
            )
            null
        }

        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, "feature-$featureName", "Playwright E2E")
        }
    }
}
