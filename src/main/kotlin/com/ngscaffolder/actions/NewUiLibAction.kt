package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.ngscaffolder.dialogs.PreviewEntry
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.UiLibGenerator
import com.ngscaffolder.util.NamingUtils

class NewUiLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New UI Library",
            "e.g. buttons → example standalone component",
            showPrefix = true
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val kebab = NamingUtils.toKebabCase(name)
        val prefix = dialog.prefix.trim()
        val workspaceRoot = findWorkspaceRoot(directory)
        if (workspaceRoot == null) {
            showNxNotFound(project)
            return
        }

        val tools = detectWorkspaceTools(workspaceRoot)
        val relativePath = getRelativePath(workspaceRoot, directory) + "/$kebab"
        val generator = getConfiguredGenerator()
        val nxArgs = buildNxArgs(
            name = kebab,
            relativePath = relativePath,
            prefix = prefix,
            style = "scss",
            tools = tools,
        )

        val preview = runNxDryRun(project, workspaceRoot, generator, nxArgs)
        if (preview == null || !preview.success) {
            showNxError(project, preview)
            return
        }
        val parsed = parseDryRunOutput(preview.output)
        val nxLibRoot = extractLibRoot(parsed) ?: relativePath
        val flatEntries = flattenPreviewEntries(filterCleanedFiles(parsed), nxLibRoot, relativePath) + listOf(
            PreviewEntry("CREATE", "$relativePath/src/lib/$kebab/$kebab.component.ts"),
            PreviewEntry("CREATE", "$relativePath/src/lib/$kebab/$kebab.component.html"),
            PreviewEntry("CREATE", "$relativePath/src/lib/$kebab/$kebab.component.scss"),
            PreviewEntry("CREATE", "$relativePath/src/lib/$kebab/$kebab.component.spec.ts"),
            PreviewEntry("UPDATE", "$relativePath/src/index.ts"),
        )
        if (!showTreePreview(project, flatEntries)) return

        val snapshot = snapshotWorkspaceFiles(workspaceRoot)
        val result = runNxGenerate(project, workspaceRoot, generator, nxArgs)
        if (result == null || !result.success) {
            showNxError(project, result)
            return
        }

        VfsUtil.markDirtyAndRefresh(false, true, true, directory)
        com.intellij.openapi.application.WriteAction.runAndWait<Throwable> {
            flattenNestedLib(workspaceRoot, nxLibRoot, relativePath)
            restoreWorkspaceFiles(workspaceRoot, snapshot)
        }
        val libRoot = refreshAndFindLibByPath(workspaceRoot, relativePath) ?: return

        val file = runWithCleanup(project, libRoot) {
            cleanNxDefaultFiles(libRoot)
            UiLibGenerator(project).generate(libRoot, name, prefix)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "UI")
        }
    }
}
