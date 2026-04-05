package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.NxLibraryDialog
import com.ngscaffolder.generators.NxLibraryGenerator

class NewNxLibraryAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val directory = getTargetDirectory(e) ?: return

        val dialog = NxLibraryDialog()
        if (!dialog.showAndGet()) return

        val domain = dialog.domain.trim()
        val libType = dialog.libType.trim()
        val libName = dialog.libName.trim()
        if (domain.isEmpty() || libType.isEmpty() || libName.isEmpty()) return

        runWriteAction {
            NxLibraryGenerator().generate(directory, domain, libType, libName)
        }
    }
}
