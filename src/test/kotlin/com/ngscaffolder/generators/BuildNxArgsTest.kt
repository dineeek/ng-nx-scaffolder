package com.ngscaffolder.generators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BuildNxArgsTest {

    private fun buildArgs(
        name: String = "my-lib",
        relativePath: String = "libs/domain/my-lib",
        prefix: String? = null,
        style: String = "none",
        tools: WorkspaceTools = WorkspaceTools(
            hasEslint = true,
            hasStylelint = false,
            hasPrettier = false,
            testRunner = TestRunner.JEST,
        ),
    ): List<String> {
        val args = mutableListOf(
            "--name=$name",
            "--directory=$relativePath",
            "--standalone",
        )
        if (prefix != null) args.add("--prefix=$prefix")
        if (style != "none") args.add("--style=$style")
        if (!tools.hasEslint) args.add("--linter=none")
        args.add("--unitTestRunner=${tools.testRunner.cliValue}")
        return args
    }

    @Test
    fun `includes name and directory`() {
        val args = buildArgs(name = "my-feature", relativePath = "libs/sales/my-feature")
        assertTrue(args.contains("--name=my-feature"))
        assertTrue(args.contains("--directory=libs/sales/my-feature"))
        assertTrue(args.contains("--standalone"))
    }

    @Test
    fun `includes prefix when provided`() {
        val args = buildArgs(prefix = "app")
        assertTrue(args.contains("--prefix=app"))
    }

    @Test
    fun `omits prefix when null`() {
        val args = buildArgs(prefix = null)
        assertFalse(args.any { it.startsWith("--prefix=") })
    }

    @Test
    fun `includes style when not none`() {
        val args = buildArgs(style = "scss")
        assertTrue(args.contains("--style=scss"))
    }

    @Test
    fun `omits style when none`() {
        val args = buildArgs(style = "none")
        assertFalse(args.any { it.startsWith("--style=") })
    }

    @Test
    fun `adds linter none when eslint absent`() {
        val tools = WorkspaceTools(hasEslint = false, hasStylelint = false, hasPrettier = false, testRunner = TestRunner.JEST)
        val args = buildArgs(tools = tools)
        assertTrue(args.contains("--linter=none"))
    }

    @Test
    fun `omits linter none when eslint present`() {
        val tools = WorkspaceTools(hasEslint = true, hasStylelint = false, hasPrettier = false, testRunner = TestRunner.JEST)
        val args = buildArgs(tools = tools)
        assertFalse(args.contains("--linter=none"))
    }

    @Test
    fun `sets jest test runner`() {
        val tools = WorkspaceTools(hasEslint = true, hasStylelint = false, hasPrettier = false, testRunner = TestRunner.JEST)
        val args = buildArgs(tools = tools)
        assertTrue(args.contains("--unitTestRunner=jest"))
    }

    @Test
    fun `sets vitest test runner`() {
        val tools = WorkspaceTools(hasEslint = true, hasStylelint = false, hasPrettier = false, testRunner = TestRunner.VITEST)
        val args = buildArgs(tools = tools)
        assertTrue(args.contains("--unitTestRunner=vitest"))
    }

    @Test
    fun `sets none test runner`() {
        val tools = WorkspaceTools(hasEslint = true, hasStylelint = false, hasPrettier = false, testRunner = TestRunner.NONE)
        val args = buildArgs(tools = tools)
        assertTrue(args.contains("--unitTestRunner=none"))
    }
}
