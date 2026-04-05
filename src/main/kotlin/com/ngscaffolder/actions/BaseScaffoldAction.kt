package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile

abstract class BaseScaffoldAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null
    }

    protected fun getTargetDirectory(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        return if (file.isDirectory) file else file.parent
    }

    protected fun openFileInEditor(e: AnActionEvent, file: VirtualFile) {
        val project = e.project ?: return
        FileEditorManager.getInstance(project).openFile(file, true)
    }

    protected fun runWriteAction(action: () -> VirtualFile?): VirtualFile? {
        var result: VirtualFile? = null
        WriteAction.runAndWait<Throwable> {
            result = action()
        }
        return result
    }
}
