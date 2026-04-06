package com.ngscaffolder.actions

import com.intellij.openapi.actionSystem.AnActionEvent
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
        val nxArgs = buildNxArgs(name = kebab, relativePath = relativePath, tools = tools)

        val result = runNxGenerate(project, workspaceRoot, "@nx/angular:library", nxArgs)
        if (result == null || !result.success) {
            showNxError(project, result)
            return
        }

        val libRoot = refreshAndFindLibDir(directory, kebab) ?: return

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
