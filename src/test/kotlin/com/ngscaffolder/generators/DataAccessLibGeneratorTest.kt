package com.ngscaffolder.generators

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DataAccessLibGeneratorTest : BasePlatformTestCase() {

    private fun createLibRoot(): VirtualFile {
        val root = myFixture.tempDirFixture.findOrCreateDir("data-access-lib")
        return WriteAction.computeAndWait<VirtualFile, Throwable> {
            val src = root.createChildDirectory(this, "src")
            src.createChildData(this, "index.ts")
            src.createChildDirectory(this, "lib")
            root
        }
    }

    fun `test generates service and spec`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            DataAccessLibGenerator(project).generate(libRoot, "users")
        }

        val services = libRoot.findChild("src")!!.findChild("lib")!!.findChild("services")
        assertNotNull(services)
        assertNotNull(services!!.findChild("users.service.ts"))
        assertNotNull(services.findChild("users.service.spec.ts"))
    }

    fun `test overwrites index with service export`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            DataAccessLibGenerator(project).generate(libRoot, "users")
        }

        val indexContent = String(libRoot.findChild("src")!!.findChild("index.ts")!!.contentsToByteArray())
        assertTrue(indexContent.contains("UsersService"))
        assertTrue(indexContent.contains("users.service"))
    }
}
