package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.settings.PluginSettings
import com.ngscaffolder.util.NamingUtils
import java.util.*

class UiLibGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, name: String, prefix: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)

        val props = Properties().apply {
            setProperty("PREFIX", prefix)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val srcDir = directory.createChildDirectory(this, "src")
        val libDir = srcDir.createChildDirectory(this, "lib")
        val exampleDir = libDir.createChildDirectory(this, "example")

        // Config files
        ConfigFileGenerator(project).generate(
            directory = directory,
            srcDir = srcDir,
            libName = kebab,
            prefix = prefix,
            libType = "ui",
            hasSpecs = true,
            hasNgPackage = true,
            hasStyles = true,
        )

        val componentTpl = templateManager.getInternalTemplate("Ui Example Component")
        createFile(exampleDir, "example.component.ts", componentTpl.getText(props))

        val htmlTpl = templateManager.getInternalTemplate("Ui Example Component HTML")
        createFile(exampleDir, "example.component.html", htmlTpl.getText(props))
        createFile(exampleDir, "example.component.scss", "")

        createFile(srcDir, "index.ts", "export { ExampleComponent } from './lib/example/example.component'\n")

        return exampleDir.findChild("example.component.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
