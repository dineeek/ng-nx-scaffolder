package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.FeatureLibDialog
import com.ngscaffolder.generators.FeatureLibGenerator
import com.ngscaffolder.generators.FeatureLibOptions

class NewFeatureLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = FeatureLibDialog()
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val options = FeatureLibOptions(
            name = name,
            domain = dialog.domain.trim(),
            prefix = dialog.prefix.trim(),
            hasStore = dialog.hasStore,
            hasFacade = dialog.hasFacade,
            hasForm = dialog.hasForm,
            hasRouting = dialog.hasRouting && !dialog.isDialog,
            isDialog = dialog.isDialog,
        )

        val file = runWriteAction {
            FeatureLibGenerator(project).generate(directory, options)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
