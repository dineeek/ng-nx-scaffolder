package com.ngscaffolder.generators

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile

data class NxResult(val success: Boolean, val output: String)

enum class TestRunner(val cliValue: String) {
    JEST("jest"),
    VITEST("vitest"),
    NONE("none"),
}

data class WorkspaceTools(
    val hasEslint: Boolean,
    val hasStylelint: Boolean,
    val hasPrettier: Boolean,
    val testRunner: TestRunner,
    val nxMajorVersion: Int,
)

class NxCliRunner {

    private val log = Logger.getInstance(NxCliRunner::class.java)

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
            testRunner = detectTestRunner(workspaceRoot),
            nxMajorVersion = detectNxMajorVersion(workspaceRoot),
        )
    }

    private fun detectTestRunner(workspaceRoot: VirtualFile): TestRunner {
        val hasVitest = hasAnyFile(workspaceRoot,
            "vitest.config.ts", "vitest.config.js", "vitest.config.mts",
            "vitest.workspace.ts", "vitest.workspace.js")
        val hasJest = hasAnyFile(workspaceRoot,
            "jest.config.ts", "jest.config.js", "jest.preset.js", "jest.preset.ts")

        return when {
            hasVitest && !hasJest -> TestRunner.VITEST
            hasJest && !hasVitest -> TestRunner.JEST
            hasVitest && hasJest -> TestRunner.JEST
            else -> TestRunner.NONE
        }
    }

    private fun detectNxMajorVersion(workspaceRoot: VirtualFile): Int {
        try {
            val pkgJson = workspaceRoot.findChild("node_modules")
                ?.findChild("nx")?.findChild("package.json") ?: return 0
            val content = String(pkgJson.contentsToByteArray())
            val versionMatch = Regex("\"version\"\\s*:\\s*\"(\\d+)").find(content)
            return versionMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            return 0
        }
    }

    fun runGenerate(
        workspaceRoot: VirtualFile,
        generator: String,
        args: List<String>,
        indicator: ProgressIndicator? = null,
    ): NxResult {
        val allArgs = mutableListOf("generate", generator)
        allArgs.addAll(args)
        allArgs.add("--no-interactive")
        allArgs.add("--skip-format")
        allArgs.add("--skipPackageJson")

        val nxJs = findNxJsEntry(workspaceRoot)
        val nodePath = if (nxJs != null) resolveNodePath() else null
        log.info("NgNxScaffolder: nxJs=$nxJs, nodePath=$nodePath")
        val commandLine = if (nxJs != null && nodePath != null) {
            GeneralCommandLine(nodePath)
                .withParameters(listOf(nxJs) + allArgs)
        } else {
            GeneralCommandLine("/bin/sh")
                .withParameters(listOf("-l", "-c", "npx nx " + allArgs.joinToString(" ") { "'$it'" }))
        }
        commandLine
            .withWorkDirectory(workspaceRoot.path)
            .withCharset(Charsets.UTF_8)
        commandLine.environment.putAll(shellEnvironment())
        log.info("NgNxScaffolder: command=${commandLine.commandLineString}")

        return try {
            val handler = CapturingProcessHandler(commandLine)
            if (indicator != null) {
                handler.addProcessListener(object : ProcessListener {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        val text = event.text.trim()
                        if (text.isNotEmpty()) {
                            indicator.text2 = text
                        }
                    }
                })
            }
            val result = handler.runProcess(120_000)
            val combinedOutput = result.stdout + result.stderr
            val isDeprecationWarning = result.exitCode == 1
                && combinedOutput.contains("In Nx 19, generating projects will no longer derive")
            val success = result.exitCode == 0 || isDeprecationWarning
            val output = if (success) result.stdout else result.stderr.ifBlank { result.stdout }
            log.info("NgNxScaffolder: exitCode=${result.exitCode}, success=$success, output=${output.take(500)}")
            NxResult(success = success, output = output)
        } catch (e: Exception) {
            NxResult(success = false, output = e.message ?: "Failed to run nx command")
        }
    }

    private fun resolveNodePath(): String? {
        // Prefer the user's interactive shell-resolved node (nvm/fnm/volta) over system installs,
        // which can break after brew upgrade (e.g. icu4c version mismatch).
        // IntelliJ launched from Dock has a minimal env — nvm init is in .zshrc (interactive),
        // so we must use zsh -i -l to load it.
        val shells = listOf("/bin/zsh", "/bin/bash", "/bin/sh")
        for (shell in shells) {
            if (!java.io.File(shell).canExecute()) continue
            try {
                val proc = ProcessBuilder(shell, "-i", "-l", "-c", "which node")
                    .redirectErrorStream(true)
                    .start()
                val path = proc.inputStream.bufferedReader().readText().trim()
                proc.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
                val isValid = proc.exitValue() == 0 && path.isNotEmpty()
                    && !path.contains("not found")
                if (isValid && java.io.File(path).canExecute()) {
                    log.info("NgNxScaffolder: resolved node via $shell -> $path")
                    return path
                }
            } catch (_: Exception) { /* try next shell */ }
        }

        // Direct lookup for nvm/fnm/volta managed node
        val home = System.getProperty("user.home")
        val managerPaths = listOf(
            "$home/.nvm/current/bin/node",
            "$home/.local/share/fnm/aliases/default/bin/node",
            "$home/.volta/bin/node",
        )
        managerPaths.firstOrNull { java.io.File(it).canExecute() }?.let {
            log.info("NgNxScaffolder: resolved node via manager path -> $it")
            return it
        }

        // Find latest nvm-installed node as last nvm fallback
        val nvmVersionsDir = java.io.File("$home/.nvm/versions/node")
        if (nvmVersionsDir.isDirectory) {
            val latest = nvmVersionsDir.listFiles()
                ?.filter { java.io.File(it, "bin/node").canExecute() }
                ?.maxByOrNull { it.name }
            if (latest != null) {
                val path = "${latest.path}/bin/node"
                log.info("NgNxScaffolder: resolved node via nvm versions scan -> $path")
                return path
            }
        }

        // System node as absolute last resort
        val systemPaths = listOf("/usr/local/bin/node", "/opt/homebrew/bin/node")
        return systemPaths.firstOrNull { java.io.File(it).canExecute() }?.also {
            log.info("NgNxScaffolder: resolved node via system path -> $it")
        }
    }

    private fun shellEnvironment(): Map<String, String> {
        return try {
            val shell = listOf("/bin/zsh", "/bin/bash", "/bin/sh")
                .first { java.io.File(it).canExecute() }
            val proc = ProcessBuilder(shell, "-i", "-l", "-c", "env")
                .redirectErrorStream(true)
                .start()
            val env = mutableMapOf<String, String>()
            proc.inputStream.bufferedReader().forEachLine { line ->
                val idx = line.indexOf('=')
                if (idx > 0) {
                    env[line.substring(0, idx)] = line.substring(idx + 1)
                }
            }
            proc.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            env
        } catch (_: Exception) {
            System.getenv()
        }
    }

    private fun findNxJsEntry(workspaceRoot: VirtualFile): String? {
        val nxDir = workspaceRoot.findChild("node_modules")?.findChild("nx") ?: return null
        val binDir = nxDir.findChild("bin") ?: return null
        val nxJs = binDir.findChild("nx.js") ?: return null
        return nxJs.path
    }

    private fun hasAnyFile(dir: VirtualFile, vararg names: String): Boolean {
        return names.any { dir.findChild(it) != null }
    }
}
