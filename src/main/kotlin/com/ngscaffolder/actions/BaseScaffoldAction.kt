package com.ngscaffolder.actions

import com.intellij.ide.IdeView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.generators.NxCliRunner
import com.ngscaffolder.generators.NxResult
import com.ngscaffolder.generators.WorkspaceTools

abstract class BaseScaffoldAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val hasFile = e.getData(CommonDataKeys.VIRTUAL_FILE) != null
        val hasIdeView = e.getData(LangDataKeys.IDE_VIEW)?.directories?.isNotEmpty() == true
        e.presentation.isEnabledAndVisible = hasFile || hasIdeView
    }

    protected fun getTargetDirectory(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null) {
            return if (file.isDirectory) file else file.parent
        }
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

    protected fun findWorkspaceRoot(directory: VirtualFile): VirtualFile? {
        return NxCliRunner().findWorkspaceRoot(directory)
    }

    protected fun detectWorkspaceTools(workspaceRoot: VirtualFile): WorkspaceTools {
        return NxCliRunner().detectWorkspaceTools(workspaceRoot)
    }

    protected fun getRelativePath(workspaceRoot: VirtualFile, directory: VirtualFile): String {
        return VfsUtil.getRelativePath(directory, workspaceRoot) ?: directory.name
    }

    protected fun buildNxArgs(
        name: String,
        relativePath: String,
        prefix: String? = null,
        style: String = "none",
        tools: WorkspaceTools,
    ): List<String> {
        val args = mutableListOf(
            "--name=$name",
            "--directory=$relativePath",
            "--standalone",
        )
        if (prefix != null) args.add("--prefix=$prefix")
        if (style != "none") args.add("--style=$style")
        if (!tools.hasEslint) args.add("--linter=none")
        return args
    }

    protected fun runNxGenerate(
        project: Project,
        workspaceRoot: VirtualFile,
        generator: String,
        args: List<String>,
    ): NxResult? {
        var nxResult: NxResult? = null
        val completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
            { nxResult = NxCliRunner().runGenerate(workspaceRoot, generator, args) },
            "Running nx generate...",
            true,
            project,
        )
        return if (completed) nxResult else null
    }

    protected fun showNxError(project: Project, result: NxResult?) {
        val message = result?.output ?: "Operation was cancelled."
        Messages.showErrorDialog(project, message, "Nx Generate Failed")
    }

    protected fun showNxNotFound(project: Project) {
        Messages.showErrorDialog(
            project,
            "Could not find nx.json in any parent directory.\nMake sure you are inside an Nx workspace.",
            "Nx Workspace Not Found"
        )
    }

    protected fun refreshAndFindLibDir(directory: VirtualFile, libName: String): VirtualFile? {
        VfsUtil.markDirtyAndRefresh(false, true, true, directory)
        return directory.findChild(libName)
    }

    protected fun cleanNxDefaultFiles(libDir: VirtualFile) {
        val srcLib = libDir.findChild("src")?.findChild("lib") ?: return
        for (child in srcLib.children) {
            child.delete(this)
        }
    }
}
