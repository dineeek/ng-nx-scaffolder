package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

class UiLibGenerator(private val project: Project) {

    fun generate(libRoot: VirtualFile, name: String, prefix: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)

        val props = Properties().apply {
            setProperty("PREFIX", prefix)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val srcDir = libRoot.findChild("src")!!
        val libDir = srcDir.findChild("lib")!!
        val exampleDir = libDir.createChildDirectory(this, "example")

        val componentTpl = templateManager.getInternalTemplate("Ui Example Component")
        createFile(exampleDir, "example.component.ts", componentTpl.getText(props))

        val htmlTpl = templateManager.getInternalTemplate("Ui Example Component HTML")
        createFile(exampleDir, "example.component.html", htmlTpl.getText(props))
        createFile(exampleDir, "example.component.scss", "")

        overwriteFile(srcDir, "index.ts", "export { ExampleComponent } from './lib/example/example.component'\n")

        return exampleDir.findChild("example.component.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }

    private fun overwriteFile(dir: VirtualFile, fileName: String, content: String) {
        val existing = dir.findChild(fileName)
        if (existing != null) {
            existing.setBinaryContent(content.toByteArray())
        } else {
            createFile(dir, fileName, content)
        }
    }
}
