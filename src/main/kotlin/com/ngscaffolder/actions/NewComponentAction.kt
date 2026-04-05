package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.ComponentDialog
import com.ngscaffolder.generators.ComponentGenerator

class NewComponentAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = ComponentDialog()
        if (!dialog.showAndGet()) return

        val name = dialog.componentName.trim()
        if (name.isEmpty()) return

        val file = runWriteAction {
            ComponentGenerator(project).generate(directory, name)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
