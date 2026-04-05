package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.DataAccessServiceDialog
import com.ngscaffolder.generators.DataAccessServiceGenerator

class NewDataAccessServiceAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = DataAccessServiceDialog()
        if (!dialog.showAndGet()) return

        val serviceName = dialog.serviceName.trim()
        val entityName = dialog.entityName.trim()
        if (serviceName.isEmpty() || entityName.isEmpty()) return

        val file = runWriteAction {
            DataAccessServiceGenerator(project).generate(directory, serviceName, entityName)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
