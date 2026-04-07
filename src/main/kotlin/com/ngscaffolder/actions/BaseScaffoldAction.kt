package com.ngscaffolder.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.dialogs.DryRunPreviewDialog
import com.ngscaffolder.dialogs.PreviewEntry
import com.ngscaffolder.generators.NxCliRunner
import com.ngscaffolder.generators.NxResult
import com.ngscaffolder.generators.TestRunner
import com.ngscaffolder.generators.WorkspaceTools
import com.ngscaffolder.settings.PluginSettings

@Suppress("TooManyFunctions")
abstract class BaseScaffoldAction : AnAction() {

    private val log = Logger.getInstance(BaseScaffoldAction::class.java)

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

    protected fun runWithCleanup(project: Project, libRoot: VirtualFile, action: () -> VirtualFile?): VirtualFile? {
        return try {
            runWriteAction(action)
        } catch (e: Exception) {
            log.warn("Custom file generation failed, cleaning up: ${e.message}", e)
            try {
                WriteAction.runAndWait<Throwable> { libRoot.delete(this) }
            } catch (deleteEx: Exception) {
                log.warn("Failed to clean up lib directory: ${deleteEx.message}", deleteEx)
            }
            Messages.showErrorDialog(
                project,
                "Failed to generate custom files: ${e.message}\nThe library directory has been removed.",
                "Generation Failed"
            )
            null
        }
    }

    protected fun showSuccessNotification(project: Project, libName: String, libType: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("NgNxScaffolder.Notifications")
            .createNotification(
                "Library created",
                "\"$libName\" $libType library generated successfully",
                NotificationType.INFORMATION,
            )
            .notify(project)
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

    protected fun validateTargetDirectory(project: Project, workspaceRoot: VirtualFile, directory: VirtualFile): Boolean {
        val relativePath = VfsUtil.getRelativePath(directory, workspaceRoot) ?: ""
        if (!relativePath.startsWith("libs")) {
            Messages.showErrorDialog(project, "Libraries must be created inside the 'libs' folder.", "Invalid Location")
            return false
        }
        if (directory.findChild("src") != null || directory.findChild("project.json") != null) {
            Messages.showErrorDialog(project, "This folder already contains a library.", "Invalid Location")
            return false
        }
        return true
    }

    protected fun libAlreadyExists(project: Project, directory: VirtualFile, kebab: String): Boolean {
        if (directory.findChild(kebab) != null) {
            Messages.showErrorDialog(project, "Library '$kebab' already exists in this directory.", "Duplicate Library")
            return true
        }
        return false
    }

    protected fun buildNxArgs(
        name: String,
        relativePath: String,
        prefix: String? = null,
        style: String = "none",
        tools: WorkspaceTools,
        publishable: Boolean = false,
        importPath: String? = null,
    ): List<String> {
        val args = mutableListOf(
            name,
            "--directory=$relativePath",
            "--standalone",
        )
        if (prefix != null) args.add("--prefix=$prefix")
        if (style != "none") args.add("--style=$style")
        if (!tools.hasEslint) args.add("--linter=none")
        args.add("--unitTestRunner=${tools.testRunner.cliValue}")
        if (publishable) {
            args.add("--publishable")
            args.add("--importPath=${importPath ?: name}")
        }
        return args
    }

    protected fun buildJsLibNxArgs(
        name: String,
        relativePath: String,
        tools: WorkspaceTools,
        skipTests: Boolean = false,
        publishable: Boolean = false,
        importPath: String? = null,
    ): List<String> {
        val args = mutableListOf(
            name,
            "--directory=$relativePath",
        )
        if (!tools.hasEslint) args.add("--linter=none")
        args.add("--unitTestRunner=${if (skipTests) "none" else tools.testRunner.cliValue}")
        if (publishable) {
            args.add("--publishable")
            args.add("--importPath=${importPath ?: name}")
        }
        return args
    }

    protected fun fixTestSetup(libRoot: VirtualFile) {
        val testSetup = libRoot.findChild("src")?.findChild("test-setup.ts") ?: return
        testSetup.setBinaryContent("import 'jest-preset-angular/setup-jest'\n".toByteArray())
    }

    protected fun detectNpmScope(workspaceRoot: VirtualFile): String? {
        val packageJson = java.io.File(workspaceRoot.path, "package.json")
        if (!packageJson.exists()) return null
        val match = Regex("\"name\"\\s*:\\s*\"(@[^/]+)/").find(packageJson.readText())
        return match?.groupValues?.get(1)
    }

    protected fun getConfiguredGenerator(): String {
        return PluginSettings.getInstance().state.nxGenerator
    }

    protected fun snapshotWorkspaceFiles(workspaceRoot: VirtualFile): Map<String, ByteArray> {
        val filesToPreserve = listOf(".prettierignore", "nx.json")
        val snapshot = mutableMapOf<String, ByteArray>()
        for (name in filesToPreserve) {
            val diskFile = java.io.File(workspaceRoot.path, name)
            if (diskFile.exists()) snapshot[name] = diskFile.readBytes()
        }
        return snapshot
    }

    protected fun restoreWorkspaceFiles(workspaceRoot: VirtualFile, snapshot: Map<String, ByteArray>) {
        for ((name, content) in snapshot) {
            java.io.File(workspaceRoot.path, name).writeBytes(content)
            workspaceRoot.findChild(name)?.refresh(false, false)
        }
    }

    protected fun runNxGenerate(
        project: Project,
        workspaceRoot: VirtualFile,
        generator: String,
        args: List<String>,
    ): NxResult? {
        var nxResult: NxResult? = null
        val completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                val indicator = ProgressManager.getInstance().progressIndicator
                indicator?.isIndeterminate = true
                indicator?.text = "Running nx generate..."
                nxResult = NxCliRunner().runGenerate(workspaceRoot, generator, args, indicator)
            },
            "Running nx generate...",
            true,
            project,
        )
        return if (completed) nxResult else null
    }

    protected fun runNxDryRun(
        project: Project,
        workspaceRoot: VirtualFile,
        generator: String,
        args: List<String>,
    ): NxResult? {
        val dryRunArgs = args + "--dry-run"
        var nxResult: NxResult? = null
        val completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                val indicator = ProgressManager.getInstance().progressIndicator
                indicator?.isIndeterminate = true
                indicator?.text = "Previewing changes..."
                nxResult = NxCliRunner().runGenerate(workspaceRoot, generator, dryRunArgs, indicator)
            },
            "Previewing changes...",
            true,
            project,
        )
        return if (completed) nxResult else null
    }

    protected fun parseDryRunOutput(output: String): List<PreviewEntry> {
        val regex = Regex("^(CREATE|UPDATE)\\s+(.+)$")
        return output.lines()
            .mapNotNull { regex.matchEntire(it.trim()) }
            .map { PreviewEntry(it.groupValues[1], it.groupValues[2]) }
    }

    protected fun filterCleanedFiles(entries: List<PreviewEntry>): List<PreviewEntry> {
        // Matches Angular component defaults and JS lib defaults in src/lib/
        val cleanedPattern = Regex(".*/src/lib/[^/]+(/.*)?\\.((component\\.(ts|html|css|scss|spec\\.ts))|ts)$")
        // Workspace root files we restore after generation
        val restoredFiles = setOf(".prettierignore", "nx.json")
        return entries.filter { entry ->
            val fileName = entry.path.substringAfterLast("/")
            !(entry.operation == "CREATE" && cleanedPattern.matches(entry.path))
                && !(entry.operation == "UPDATE" && fileName in restoredFiles)
        }
    }

    protected fun extractLibRoot(entries: List<PreviewEntry>): String? {
        return entries.firstOrNull { it.path.contains("/src/") }
            ?.path?.substringBefore("/src/")
    }

    protected fun showDryRunPreview(project: Project, output: String): Boolean {
        val entries = parseDryRunOutput(output)
        return showTreePreview(project, entries)
    }

    protected fun showTreePreview(project: Project, entries: List<PreviewEntry>): Boolean {
        if (entries.isEmpty()) return false
        return DryRunPreviewDialog(project, entries).showAndGet()
    }

    protected fun showNxError(project: Project, result: NxResult?) {
        val message = result?.output ?: "Operation was cancelled."
        val hint = detectErrorHint(message)
        val fullMessage = if (hint != null) "$message\n\nHint: $hint" else message
        Messages.showErrorDialog(project, fullMessage, "Nx Generate Failed")
    }

    private fun detectErrorHint(output: String): String? = when {
        "ERR_INVALID_THIS" in output -> "This is an npm/npx cache error. Try running: npx clear-npx-cache"
        "ENOENT" in output && "nx" in output.lowercase() -> "Nx binary not found. Ensure node_modules is installed."
        "ERR_MODULE_NOT_FOUND" in output -> "A required module is missing. Try running: npm install"
        else -> null
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

    protected fun refreshAndFindLibByPath(workspaceRoot: VirtualFile, nxLibRoot: String): VirtualFile? {
        VfsUtil.markDirtyAndRefresh(false, true, true, workspaceRoot)
        var dir: VirtualFile? = workspaceRoot
        for (segment in nxLibRoot.split("/")) {
            dir = dir?.findChild(segment) ?: return null
        }
        return dir
    }

    protected fun flattenPreviewEntries(entries: List<PreviewEntry>, nxLibRoot: String, targetPath: String): List<PreviewEntry> {
        if (nxLibRoot == targetPath) return entries
        return entries.map { entry ->
            if (entry.path.startsWith(nxLibRoot)) {
                entry.copy(path = targetPath + entry.path.removePrefix(nxLibRoot))
            } else {
                entry
            }
        }
    }

    protected fun flattenNestedLib(workspaceRoot: VirtualFile, nxLibRoot: String, targetPath: String) {
        if (nxLibRoot == targetPath) return
        val nestedDir = findByPath(workspaceRoot, nxLibRoot) ?: return
        val targetDir = findByPath(workspaceRoot, targetPath) ?: return
        for (child in nestedDir.children) {
            child.move(this, targetDir)
        }
        nestedDir.delete(this)
        fixFlattenedPaths(targetDir, nxLibRoot, targetPath)
        fixWorkspaceRootPaths(workspaceRoot, nxLibRoot, targetPath)
    }

    private fun fixFlattenedPaths(libDir: VirtualFile, nxLibRoot: String, targetPath: String) {
        // Nx generated paths for the nested location — fix them to the flattened one
        val nxDepth = nxLibRoot.count { it == '/' } + 1  // e.g. libs/user/user = 3
        val targetDepth = targetPath.count { it == '/' } + 1  // e.g. libs/user = 2
        val extraLevels = nxDepth - targetDepth
        if (extraLevels <= 0) return

        val oldRelative = "../".repeat(nxDepth)      // e.g. ../../../
        val newRelative = "../".repeat(targetDepth)    // e.g. ../../

        val configFiles = listOf(
            "tsconfig.json", "tsconfig.lib.json", "tsconfig.lib.prod.json",
            "tsconfig.spec.json", "jest.config.ts", "project.json",
            "ng-package.json", "package.json", "README.md",
        )
        for (fileName in configFiles) {
            val file = libDir.findChild(fileName) ?: continue
            var content = String(file.contentsToByteArray())
            content = content.replace(oldRelative, newRelative)
            content = content.replace(nxLibRoot, targetPath)
            // Fix doubled project name (e.g. "random-random" → "random")
            val nxName = nxLibRoot.substringAfterLast("/")
            val targetName = targetPath.substringAfterLast("/")
            val doubledName = "$targetName-$nxName"
            content = content.replace(doubledName, targetName)
            file.setBinaryContent(content.toByteArray())
        }
    }

    private fun fixWorkspaceRootPaths(workspaceRoot: VirtualFile, nxLibRoot: String, targetPath: String) {
        // Fix tsconfig.base.json path alias (e.g. "button/button" → "button", path libs/button/button/... → libs/button/...)
        val tsconfigFile = java.io.File(workspaceRoot.path, "tsconfig.base.json")
        if (!tsconfigFile.exists()) return
        var content = tsconfigFile.readText()
        content = content.replace(nxLibRoot, targetPath)
        // Fix doubled alias key (e.g. "@scope/button/button" → "@scope/button")
        val nxName = nxLibRoot.substringAfterLast("/")
        val targetName = targetPath.substringAfterLast("/")
        val doubledName = "$targetName/$nxName"
        content = content.replace(doubledName, targetName)
        tsconfigFile.writeText(content)
        workspaceRoot.findChild("tsconfig.base.json")?.refresh(false, false)
    }

    private fun findByPath(root: VirtualFile, relativePath: String): VirtualFile? {
        var dir: VirtualFile? = root
        for (segment in relativePath.split("/")) {
            dir = dir?.findChild(segment) ?: return null
        }
        return dir
    }

    protected fun cleanNxDefaultFiles(libDir: VirtualFile) {
        val srcLib = libDir.findChild("src")?.findChild("lib") ?: return
        for (child in srcLib.children) {
            child.delete(this)
        }
    }
}
