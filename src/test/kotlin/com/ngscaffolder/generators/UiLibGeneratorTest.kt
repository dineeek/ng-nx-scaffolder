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

    fun `test generates component files in subdirectory`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            UiLibGenerator(project).generate(libRoot, "buttons", "app")
        }

        val compDir = libRoot.findChild("src")!!
            .findChild("lib")!!.findChild("buttons")
        assertNotNull(compDir)
        assertNotNull(compDir!!.findChild("buttons.component.ts"))
        assertNotNull(compDir.findChild("buttons.component.html"))
        assertNotNull(compDir.findChild("buttons.component.scss"))
        assertNotNull(compDir.findChild("buttons.component.spec.ts"))
    }

    fun `test overwrites index with component export`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            UiLibGenerator(project).generate(libRoot, "buttons", "app")
        }

        val content = String(
            libRoot.findChild("src")!!
                .findChild("index.ts")!!.contentsToByteArray()
        )
        assertTrue(content.contains("ButtonsComponent"))
        assertTrue(content.contains("buttons/buttons.component"))
    }
}
