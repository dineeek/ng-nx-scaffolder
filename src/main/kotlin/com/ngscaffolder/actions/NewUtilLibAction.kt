package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.SimpleLibDialog
import com.ngscaffolder.generators.UtilLibGenerator
import com.ngscaffolder.util.NamingUtils

class NewUtilLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = SimpleLibDialog(
            "New Util Library",
            "e.g. date-helpers → date-helpers/date-helpers.util.ts"
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
        val generator = getConfiguredGenerator()
        val nxArgs = buildNxArgs(name = kebab, relativePath = relativePath, tools = tools)

        val preview = runNxDryRun(project, workspaceRoot, generator, nxArgs)
        if (preview == null || !preview.success) {
            showNxError(project, preview)
            return
        }
        if (!showDryRunPreview(project, preview.output)) return

        val result = runNxGenerate(project, workspaceRoot, generator, nxArgs)
        if (result == null || !result.success) {
            showNxError(project, result)
            return
        }

        val libRoot = refreshAndFindLibDir(directory, kebab) ?: return

        val file = runWithCleanup(project, libRoot) {
            cleanNxDefaultFiles(libRoot)
            UtilLibGenerator(project).generate(libRoot, name)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "util")
        }
    }
}
