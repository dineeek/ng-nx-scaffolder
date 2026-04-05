package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.SignalStoreDialog
import com.ngscaffolder.generators.SignalStoreGenerator

class NewSignalStoreAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SignalStoreDialog()
        if (!dialog.showAndGet()) return

        val name = dialog.storeName.trim()
        if (name.isEmpty()) return

        val file = runWriteAction {
            SignalStoreGenerator(project).generate(directory, name)
        }
        if (file != null) openFileInEditor(e, file)
    }
}
