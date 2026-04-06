package com.ngscaffolder.generators

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class UtilLibGeneratorTest : BasePlatformTestCase() {

    private fun createLibRoot(): VirtualFile {
        val root = myFixture.tempDirFixture.findOrCreateDir("util-lib")
        return WriteAction.computeAndWait<VirtualFile, Throwable> {
            val src = root.createChildDirectory(this, "src")
            src.createChildData(this, "index.ts")
            src.createChildDirectory(this, "lib")
            root
        }
    }

    fun `test generates util and spec`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            UtilLibGenerator(project).generate(libRoot, "date-helpers")
        }

        val utilDir = libRoot.findChild("src")!!.findChild("lib")!!.findChild("date-helpers")
        assertNotNull(utilDir)
        assertNotNull(utilDir!!.findChild("date-helpers.util.ts"))
        assertNotNull(utilDir.findChild("date-helpers.util.spec.ts"))
    }

    fun `test overwrites index with util export`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            UtilLibGenerator(project).generate(libRoot, "date-helpers")
        }

        val indexContent = String(libRoot.findChild("src")!!.findChild("index.ts")!!.contentsToByteArray())
        assertTrue(indexContent.contains("date-helpers.util"))
    }
}
