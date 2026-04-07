package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

class UiLibGenerator(private val project: Project) {

    @Suppress("UNUSED_PARAMETER")
    fun generate(libRoot: VirtualFile, name: String, prefix: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)
        val className = NamingUtils.toPascalCase(name)

        val props = Properties().apply {
            setProperty("PREFIX", prefix)
            setProperty("FILE_NAME", kebab)
            setProperty("CLASS_NAME", className)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val srcDir = libRoot.findChild("src")!!
        val libDir = srcDir.findChild("lib")!!
        val componentDir = libDir.createChildDirectory(this, kebab)

        val componentTpl = templateManager.getInternalTemplate("Ui Example Component")
        createFile(componentDir, "$kebab.component.ts", componentTpl.getText(props))

        val htmlTpl = templateManager.getInternalTemplate("Ui Example Component HTML")
        createFile(componentDir, "$kebab.component.html", htmlTpl.getText(props))
        createFile(componentDir, "$kebab.component.scss", "")

        val specTpl = templateManager.getInternalTemplate("Ui Example Component Spec")
        createFile(componentDir, "$kebab.component.spec.ts", specTpl.getText(props))

        overwriteFile(srcDir, "index.ts", "export { ${className}Component } from './lib/$kebab/$kebab.component'\n")

        return componentDir.findChild("$kebab.component.ts")!!
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
