package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.DataAccessLibGenerator

class NewDataAccessLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New Data-Access Library",
            "e.g. users → services/users.service.ts"
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val file = runWriteAction {
            DataAccessLibGenerator(project).generate(directory, name)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
