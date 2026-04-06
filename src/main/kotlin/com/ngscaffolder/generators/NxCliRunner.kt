package com.ngscaffolder.generators

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.vfs.VirtualFile

data class NxResult(val success: Boolean, val output: String)

data class WorkspaceTools(
    val hasEslint: Boolean,
    val hasStylelint: Boolean,
    val hasPrettier: Boolean,
)

class NxCliRunner {

    fun findWorkspaceRoot(startDir: VirtualFile): VirtualFile? {
        var dir: VirtualFile? = startDir
        while (dir != null) {
            if (dir.findChild("nx.json") != null) return dir
            dir = dir.parent
        }
        return null
    }

    fun detectWorkspaceTools(workspaceRoot: VirtualFile): WorkspaceTools {
        return WorkspaceTools(
            hasEslint = hasAnyFile(workspaceRoot,
                ".eslintrc.json", ".eslintrc.js", ".eslintrc.yaml", ".eslintrc.yml",
                "eslint.config.js", "eslint.config.mjs", "eslint.config.cjs"),
            hasStylelint = hasAnyFile(workspaceRoot,
                ".stylelintrc.json", ".stylelintrc.js", ".stylelintrc.yaml", ".stylelintrc.yml",
                "stylelint.config.js", "stylelint.config.mjs", "stylelint.config.cjs"),
            hasPrettier = hasAnyFile(workspaceRoot,
                ".prettierrc", ".prettierrc.json", ".prettierrc.js", ".prettierrc.yaml", ".prettierrc.yml",
                "prettier.config.js", "prettier.config.mjs", "prettier.config.cjs"),
        )
    }

    fun runGenerate(
        workspaceRoot: VirtualFile,
        generator: String,
        args: List<String>,
    ): NxResult {
        val allArgs = mutableListOf("nx", "generate", generator)
        allArgs.addAll(args)
        allArgs.add("--no-interactive")
        allArgs.add("--skip-format")

        val commandLine = GeneralCommandLine("npx")
            .withParameters(allArgs)
            .withWorkDirectory(workspaceRoot.path)
            .withCharset(Charsets.UTF_8)

        return try {
            val handler = CapturingProcessHandler(commandLine)
            val result = handler.runProcess(120_000)
            NxResult(
                success = result.exitCode == 0,
                output = if (result.exitCode == 0) result.stdout else result.stderr.ifBlank { result.stdout }
            )
        } catch (e: Exception) {
            NxResult(success = false, output = e.message ?: "Failed to run nx command")
        }
    }

    private fun hasAnyFile(dir: VirtualFile, vararg names: String): Boolean {
        return names.any { dir.findChild(it) != null }
    }
}
