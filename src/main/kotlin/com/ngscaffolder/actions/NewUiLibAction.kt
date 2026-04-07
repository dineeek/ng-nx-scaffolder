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

        val workspaceRoot = findWorkspaceRoot(directory)
        if (workspaceRoot == null) {
            showNxNotFound(project)
            return
        }
        if (!validateTargetDirectory(project, workspaceRoot, directory)) return
        val scope = detectNpmScope(workspaceRoot)

        val dialog = SimpleLibDialog(
            "New UI Library",
            "e.g. button → button.component.ts",
            showPrefix = true
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val kebab = NamingUtils.toKebabCase(name)
        if (libAlreadyExists(project, directory, kebab)) return
        val prefix = dialog.prefix.trim()
        val importPath = scope?.let { "$it/$kebab" } ?: kebab
        val tools = detectWorkspaceTools(workspaceRoot)
        val relativePath = getRelativePath(workspaceRoot, directory) + "/$kebab"
        val generator = getConfiguredGenerator()
        val nxArgs = buildNxArgs(
            name = kebab,
            relativePath = relativePath,
            prefix = prefix,
            style = "scss",
            tools = tools,
            publishable = dialog.publishable,
            importPath = importPath,
        )

        val snapshot = snapshotWorkspaceFiles(workspaceRoot)

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

        val result = runNxGenerate(project, workspaceRoot, generator, nxArgs)
        if (result == null || !result.success) {
            showNxError(project, result)
            return
        }

        VfsUtil.markDirtyAndRefresh(false, true, true, directory, workspaceRoot)
        com.intellij.openapi.application.WriteAction.runAndWait<Throwable> {
            flattenNestedLib(workspaceRoot, nxLibRoot, relativePath)
            restoreWorkspaceFiles(workspaceRoot, snapshot)
        }
        val libRoot = refreshAndFindLibByPath(workspaceRoot, relativePath) ?: return

        val file = runWithCleanup(project, libRoot) {
            cleanNxDefaultFiles(libRoot)
            fixTestSetup(libRoot)
            UiLibGenerator(project).generate(libRoot, name, prefix)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "UI")
        }
    }
}
