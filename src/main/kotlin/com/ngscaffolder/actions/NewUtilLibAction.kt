package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.UtilLibGenerator

class NewUtilLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New Util Library",
            "e.g. date-helpers → date-helpers/date-helpers.util.ts"
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val file = runWriteAction {
            UtilLibGenerator(project).generate(directory, name)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
