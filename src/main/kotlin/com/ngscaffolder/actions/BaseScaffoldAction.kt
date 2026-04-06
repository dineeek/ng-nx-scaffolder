package com.ngscaffolder.actions

import com.intellij.ide.IdeView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile

abstract class BaseScaffoldAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        // Check if we have a directory context from either source
        val hasFile = e.getData(CommonDataKeys.VIRTUAL_FILE) != null
        val hasIdeView = e.getData(LangDataKeys.IDE_VIEW)?.directories?.isNotEmpty() == true
        e.presentation.isEnabledAndVisible = hasFile || hasIdeView
    }

    protected fun getTargetDirectory(e: AnActionEvent): VirtualFile? {
        // Try VIRTUAL_FILE first (right-click in project tree)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null) {
            return if (file.isDirectory) file else file.parent
        }

        // Try IdeView (New menu from top bar or keyboard shortcut)
        val ideView = e.getData(LangDataKeys.IDE_VIEW)
        return ideView?.orChooseDirectory?.virtualFile
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
