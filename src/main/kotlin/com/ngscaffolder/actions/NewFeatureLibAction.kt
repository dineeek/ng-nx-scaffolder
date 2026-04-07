package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.ngscaffolder.dialogs.FeatureLibDialog
import com.ngscaffolder.dialogs.PreviewEntry
import com.ngscaffolder.generators.FeatureLibGenerator
import com.ngscaffolder.generators.FeatureLibOptions
import com.ngscaffolder.util.NamingUtils

class NewFeatureLibAction : BaseScaffoldAction() {

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

        val dialog = FeatureLibDialog()
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

        val componentName = "$kebab-container"
        val customEntries = mutableListOf(
            PreviewEntry("CREATE", "$relativePath/src/lib/container/$componentName.component.ts"),
            PreviewEntry("CREATE", "$relativePath/src/lib/container/$componentName.component.html"),
            PreviewEntry("CREATE", "$relativePath/src/lib/container/$componentName.component.scss"),
            PreviewEntry("CREATE", "$relativePath/src/lib/container/$componentName.component.spec.ts"),
            PreviewEntry("CREATE", "$relativePath/src/lib/mapper/$kebab.mapper.ts"),
            PreviewEntry("CREATE", "$relativePath/src/lib/mapper/$kebab.mapper.spec.ts"),
            PreviewEntry("CREATE", "$relativePath/src/lib/models/example.model.ts"),
            PreviewEntry("UPDATE", "$relativePath/src/index.ts"),
        )
        if (dialog.hasStore) {
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/store/$kebab.store.ts"))
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/store/$kebab.state.ts"))
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/store/$kebab.store.spec.ts"))
        }
        if (dialog.hasFacade) {
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/facade/$kebab-facade.service.ts"))
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/facade/$kebab-facade.service.spec.ts"))
        }
        if (dialog.hasForm) {
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/form/$kebab-form.service.ts"))
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/form/$kebab-form.model.ts"))
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/form/$kebab-form.service.spec.ts"))
        }
        if (dialog.hasRouting && !dialog.isDialog) {
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/$kebab.routes.ts"))
        }
        if (dialog.isDialog) {
            customEntries.add(PreviewEntry("CREATE", "$relativePath/src/lib/models/$kebab-dialog.model.ts"))
        }

        val flatEntries = flattenPreviewEntries(filterCleanedFiles(parsed), nxLibRoot, relativePath) + customEntries
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
            fixTestSetup(libRoot)
            FeatureLibGenerator(project).generate(libRoot, options)
        }
        if (file != null) {
            openFileInEditor(e, file)
            showSuccessNotification(project, kebab, "feature")
        }
    }
}
