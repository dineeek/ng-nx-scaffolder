package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.ngscaffolder.dialogs.PreviewEntry
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.DataAccessLibGenerator
import com.ngscaffolder.util.NamingUtils

class NewDataAccessLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New Data-Access Library",
            "e.g. users → services/users.service.ts"
        )
        if (!dialog.showAndGet()) return

        val name = dialog.libName.trim()
        if (name.isEmpty()) return

        val kebab = NamingUtils.toKebabCase(name)
        val workspaceRoot = findWorkspaceRoot(directory)
        if (workspaceRoot == null) {
            showNxNotFound(project)
            return
        }

        val tools = detectWorkspaceTools(workspaceRoot)
        val relativePath = getRelativePath(workspaceRoot, directory) + "/$kebab"
        val generator = "@nx/js:library"
        val nxArgs = buildJsLibNxArgs(name = kebab, relativePath = relativePath, tools = tools)

        val preview = runNxDryRun(project, workspaceRoot, generator, nxArgs)
        if (preview == null || !preview.success) {
            showNxError(project, preview)
            return
        }
        val parsed = parseDryRunOutput(preview.output)
        val nxLibRoot = extractLibRoot(parsed) ?: relativePath
        val flatEntries = flattenPreviewEntries(filterCleanedFiles(parsed), nxLibRoot, relativePath) + listOf(
            PreviewEntry("CREATE", "$relativePath/src/lib/services/$kebab.service.ts"),
            PreviewEntry("CREATE", "$relativePath/src/lib/services/$kebab.service.spec.ts"),
            PreviewEntry("UPDATE", "$relativePath/src/index.ts"),
        )
        if (!showTreePreview(project, flatEntries)) return

        val result = runNxGenerate(project, workspaceRoot, generator, nxArgs)
        if (result == null || !result.success) {
            showNxError(project, result)
            return
        }

        VfsUtil.markDirtyAndRefresh(false, true, true, directory)
        com.intellij.openapi.application.WriteAction.runAndWait<Throwable> { flattenNestedLib(workspaceRoot, nxLibRoot, relativePath) }
        val libRoot = refreshAndFindLibByPath(workspaceRoot, relativePath) ?: return

        val file = runWithCleanup(project, libRoot) {
            cleanNxDefaultFiles(libRoot)
            DataAccessLibGenerator(project).generate(libRoot, name)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "data-access")
        }
    }
}
