package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.ModelLibGenerator

class NewModelLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New Model Library",
            "e.g. users → models/users.model.ts"
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val file = runWriteAction {
            ModelLibGenerator(project).generate(directory, name)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
