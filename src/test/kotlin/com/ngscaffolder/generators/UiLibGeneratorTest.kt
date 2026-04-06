package com.ngscaffolder.generators

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class UiLibGeneratorTest : BasePlatformTestCase() {

    private fun createLibRoot(): VirtualFile {
        val root = myFixture.tempDirFixture.findOrCreateDir("ui-lib")
        return WriteAction.computeAndWait<VirtualFile, Throwable> {
            val src = root.createChildDirectory(this, "src")
            src.createChildData(this, "index.ts")
            src.createChildDirectory(this, "lib")
            root
        }
    }

    fun `test generates example component files`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            UiLibGenerator(project).generate(libRoot, "buttons", "app")
        }

        val example = libRoot.findChild("src")!!.findChild("lib")!!.findChild("example")
        assertNotNull(example)
        assertNotNull(example!!.findChild("example.component.ts"))
        assertNotNull(example.findChild("example.component.html"))
        assertNotNull(example.findChild("example.component.scss"))
    }

    fun `test overwrites index with component export`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            UiLibGenerator(project).generate(libRoot, "buttons", "app")
        }

        val indexContent = String(libRoot.findChild("src")!!.findChild("index.ts")!!.contentsToByteArray())
        assertTrue(indexContent.contains("ExampleComponent"))
    }
}
