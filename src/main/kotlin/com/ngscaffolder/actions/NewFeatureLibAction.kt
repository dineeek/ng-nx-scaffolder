package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.ngscaffolder.dialogs.FeatureLibDialog
import com.ngscaffolder.generators.FeatureLibGenerator
import com.ngscaffolder.generators.FeatureLibOptions
import com.ngscaffolder.util.NamingUtils

class NewFeatureLibAction : BaseScaffoldAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = getTargetDirectory(e) ?: return

        val dialog = FeatureLibDialog()
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
        if (!showDryRunPreview(project, preview.output)) return

        val result = runNxGenerate(project, workspaceRoot, generator, nxArgs)
        if (result == null || !result.success) {
            showNxError(project, result)
            return
        }

        val libRoot = refreshAndFindLibDir(directory, kebab) ?: return

        val options = FeatureLibOptions(
            name = name,
            prefix = prefix,
            hasStore = dialog.hasStore,
            hasFacade = dialog.hasFacade,
            hasForm = dialog.hasForm,
            hasRouting = dialog.hasRouting && !dialog.isDialog,
            isDialog = dialog.isDialog,
        )

        val file = runWithCleanup(project, libRoot) {
            cleanNxDefaultFiles(libRoot)
            FeatureLibGenerator(project).generate(libRoot, options)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "feature")
        }
    }
}
