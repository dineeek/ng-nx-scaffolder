package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.ngscaffolder.dialogs.PreviewEntry
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.ModelLibGenerator
import com.ngscaffolder.util.NamingUtils

class NewModelLibAction : BaseScaffoldAction() {

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
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
            "New Model Library",
            "e.g. user → user.model.ts"
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val kebab = NamingUtils.toKebabCase(name)
        if (libAlreadyExists(project, directory, kebab)) return
        val importPath = scope?.let { "$it/$kebab" } ?: kebab
        val tools = detectWorkspaceTools(workspaceRoot)
        val relativePath = getRelativePath(workspaceRoot, directory) + "/$kebab"
        val generator = "@nx/js:library"
        val nxArgs = buildJsLibNxArgs(
            name = kebab, relativePath = relativePath, tools = tools, skipTests = true,
            publishable = dialog.publishable, importPath = importPath,
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
            PreviewEntry("CREATE", "$relativePath/src/lib/models/$kebab.model.ts"),
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
            ModelLibGenerator(project).generate(libRoot, name)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "model")
        }
    }
}
