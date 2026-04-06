package com.ngscaffolder.generators

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class FeatureLibGeneratorTest : BasePlatformTestCase() {

    private fun createLibRoot(): VirtualFile {
        val root = myFixture.tempDirFixture.findOrCreateDir("feature-lib")
        return WriteAction.computeAndWait<VirtualFile, Throwable> {
            val src = root.createChildDirectory(this, "src")
            src.createChildData(this, "index.ts")
            src.createChildDirectory(this, "lib")
            root
        }
    }

    private fun opts(
        name: String = "orders",
        hasStore: Boolean = false,
        hasFacade: Boolean = false,
        hasForm: Boolean = false,
        hasRouting: Boolean = false,
        isDialog: Boolean = false,
    ) = FeatureLibOptions(name, "app", hasStore, hasFacade, hasForm, hasRouting, isDialog)

    fun `test generates container files`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts("user-profile"))
        }

        val container = libRoot.findChild("src")!!.findChild("lib")!!.findChild("container")
        assertNotNull(container)
        assertNotNull(container!!.findChild("user-profile-container.component.ts"))
        assertNotNull(container.findChild("user-profile-container.component.html"))
        assertNotNull(container.findChild("user-profile-container.component.scss"))
        assertNotNull(container.findChild("user-profile-container.component.spec.ts"))
    }

    fun `test generates mapper and models always`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts("checkout"))
        }

        val lib = libRoot.findChild("src")!!.findChild("lib")!!
        assertNotNull(lib.findChild("mapper"))
        assertNotNull(lib.findChild("mapper")!!.findChild("checkout.mapper.ts"))
        assertNotNull(lib.findChild("mapper")!!.findChild("checkout.mapper.spec.ts"))
        assertNotNull(lib.findChild("models"))
        assertNotNull(lib.findChild("models")!!.findChild("example.model.ts"))
    }

    fun `test generates store when enabled`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts(hasStore = true))
        }

        val store = libRoot.findChild("src")!!.findChild("lib")!!.findChild("store")
        assertNotNull(store)
        assertNotNull(store!!.findChild("orders.store.ts"))
        assertNotNull(store.findChild("orders.state.ts"))
        assertNotNull(store.findChild("orders.store.spec.ts"))
    }

    fun `test no store when disabled`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts())
        }

        val lib = libRoot.findChild("src")!!.findChild("lib")!!
        assertNull(lib.findChild("store"))
    }

    fun `test generates facade when enabled`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts(hasFacade = true))
        }

        val facade = libRoot.findChild("src")!!.findChild("lib")!!.findChild("facade")
        assertNotNull(facade)
        assertNotNull(facade!!.findChild("orders-facade.service.ts"))
        assertNotNull(facade.findChild("orders-facade.service.spec.ts"))
    }

    fun `test generates form when enabled`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts(hasForm = true))
        }

        val form = libRoot.findChild("src")!!.findChild("lib")!!.findChild("form")
        assertNotNull(form)
        assertNotNull(form!!.findChild("orders-form.service.ts"))
        assertNotNull(form.findChild("orders-form.model.ts"))
        assertNotNull(form.findChild("orders-form.service.spec.ts"))
    }

    fun `test generates routes when routing enabled`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts(hasRouting = true))
        }

        val lib = libRoot.findChild("src")!!.findChild("lib")!!
        assertNotNull(lib.findChild("orders.routes.ts"))
    }

    fun `test no routes when dialog`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(
                libRoot, opts(hasRouting = true, isDialog = true)
            )
        }

        val lib = libRoot.findChild("src")!!.findChild("lib")!!
        assertNull(lib.findChild("orders.routes.ts"))
    }

    fun `test dialog generates dialog model`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts(isDialog = true))
        }

        val models = libRoot.findChild("src")!!.findChild("lib")!!.findChild("models")!!
        assertNotNull(models.findChild("orders-dialog.model.ts"))
    }

    fun `test overwrites index ts`() {
        val libRoot = createLibRoot()

        WriteAction.runAndWait<Throwable> {
            FeatureLibGenerator(project).generate(libRoot, opts())
        }

        val index = libRoot.findChild("src")!!.findChild("index.ts")!!
        val indexContent = String(index.contentsToByteArray())
        assertTrue(indexContent.contains("orders-container.component"))
    }
}
