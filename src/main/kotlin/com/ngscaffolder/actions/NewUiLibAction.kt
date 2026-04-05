package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.UiLibGenerator

class NewUiLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New UI Library",
            "e.g. buttons → example standalone component",
            showPrefix = true
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val file = runWriteAction {
            UiLibGenerator(project).generate(directory, name, dialog.prefix.trim())
        }
        if (file != null) openFileInEditor(e, file)
    }
}
