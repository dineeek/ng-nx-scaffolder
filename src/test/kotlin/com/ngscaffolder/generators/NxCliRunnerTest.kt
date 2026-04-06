package com.ngscaffolder.generators

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class NxCliRunnerTest : BasePlatformTestCase() {

    private val runner = NxCliRunner()
    private lateinit var tempRoot: File

    override fun setUp() {
        super.setUp()
        tempRoot = createTempDir("nx-test")
    }

    override fun tearDown() {
        try {
            tempRoot.deleteRecursively()
        } finally {
            super.tearDown()
        }
    }

    private fun refreshVfs(file: File): VirtualFile? {
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(file.absolutePath)
    }

    // --- findWorkspaceRoot ---

    fun testFindWorkspaceRootReturnsDirWithNxJson() {
        val wsRoot = File(tempRoot, "project").apply { mkdirs() }
        File(wsRoot, "nx.json").createNewFile()
        val deep = File(wsRoot, "libs/domain").apply { mkdirs() }

        val deepVf = refreshVfs(deep)!!
        val result = runner.findWorkspaceRoot(deepVf)
        assertEquals(wsRoot.absolutePath, result?.path)
    }

    fun testFindWorkspaceRootReturnsNullWhenNoNxJson() {
        val dir = File(tempRoot, "no-nx").apply { mkdirs() }
        val vf = refreshVfs(dir)!!
        assertNull(runner.findWorkspaceRoot(vf))
    }

    fun testFindWorkspaceRootReturnsImmediateDirIfItHasNxJson() {
        val wsRoot = File(tempRoot, "workspace").apply { mkdirs() }
        File(wsRoot, "nx.json").createNewFile()

        val vf = refreshVfs(wsRoot)!!
        val result = runner.findWorkspaceRoot(vf)
        assertEquals(wsRoot.absolutePath, result?.path)
    }

    // --- detectWorkspaceTools ---

    fun testDetectsEslintFromEslintrcJson() {
        val ws = File(tempRoot, "ws-eslint").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, ".eslintrc.json").createNewFile()

        val vf = refreshVfs(ws)!!
        assertTrue(runner.detectWorkspaceTools(vf).hasEslint)
    }

    fun testDetectsEslintFromFlatConfig() {
        val ws = File(tempRoot, "ws-eslint-flat").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, "eslint.config.js").createNewFile()

        val vf = refreshVfs(ws)!!
        assertTrue(runner.detectWorkspaceTools(vf).hasEslint)
    }

    fun testNoEslintWhenAbsent() {
        val ws = File(tempRoot, "ws-no-eslint").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()

        val vf = refreshVfs(ws)!!
        assertFalse(runner.detectWorkspaceTools(vf).hasEslint)
    }

    fun testDetectsStylelint() {
        val ws = File(tempRoot, "ws-stylelint").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, ".stylelintrc.json").createNewFile()

        val vf = refreshVfs(ws)!!
        assertTrue(runner.detectWorkspaceTools(vf).hasStylelint)
    }

    fun testDetectsPrettier() {
        val ws = File(tempRoot, "ws-prettier").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, ".prettierrc.json").createNewFile()

        val vf = refreshVfs(ws)!!
        assertTrue(runner.detectWorkspaceTools(vf).hasPrettier)
    }

    // --- test runner detection ---

    fun testDetectsJestWhenJestPresetExists() {
        val ws = File(tempRoot, "ws-jest").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, "jest.preset.js").createNewFile()

        val vf = refreshVfs(ws)!!
        assertEquals(TestRunner.JEST, runner.detectWorkspaceTools(vf).testRunner)
    }

    fun testDetectsVitestWhenVitestConfigExists() {
        val ws = File(tempRoot, "ws-vitest").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, "vitest.config.ts").createNewFile()

        val vf = refreshVfs(ws)!!
        assertEquals(TestRunner.VITEST, runner.detectWorkspaceTools(vf).testRunner)
    }

    fun testPrefersJestWhenBothExist() {
        val ws = File(tempRoot, "ws-both").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()
        File(ws, "jest.preset.js").createNewFile()
        File(ws, "vitest.config.ts").createNewFile()

        val vf = refreshVfs(ws)!!
        assertEquals(TestRunner.JEST, runner.detectWorkspaceTools(vf).testRunner)
    }

    fun testNoTestRunnerWhenNeitherExists() {
        val ws = File(tempRoot, "ws-none").apply { mkdirs() }
        File(ws, "nx.json").createNewFile()

        val vf = refreshVfs(ws)!!
        assertEquals(TestRunner.NONE, runner.detectWorkspaceTools(vf).testRunner)
    }
}
