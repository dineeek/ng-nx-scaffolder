package com.ngscaffolder.generators

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ModelLibGeneratorTest : BasePlatformTestCase() {

    private fun createLibRoot(): VirtualFile {
        val root = myFixture.tempDirFixture.findOrCreateDir("model-lib")
        return WriteAction.computeAndWait<VirtualFile, Throwable> {
            val src = root.createChildDirectory(this, "src")
            src.createChildData(this, "index.ts")
            src.createChildDirectory(this, "lib")
            root
        }
    }

    fun `test generates model file`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            ModelLibGenerator(project).generate(libRoot, "vehicle")
        }

        val models = libRoot.findChild("src")!!.findChild("lib")!!.findChild("models")
        assertNotNull(models)
        assertNotNull(models!!.findChild("vehicle.model.ts"))
    }

    fun `test overwrites index with interface export`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            ModelLibGenerator(project).generate(libRoot, "vehicle")
        }

        val indexContent = String(libRoot.findChild("src")!!.findChild("index.ts")!!.contentsToByteArray())
        assertTrue(indexContent.contains("IVehicle"))
        assertTrue(indexContent.contains("vehicle.model"))
    }
}
