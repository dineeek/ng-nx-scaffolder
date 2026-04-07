package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

class DataAccessLibGenerator(private val project: Project) {

    fun generate(libRoot: VirtualFile, name: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)
        val className = NamingUtils.toPascalCase(name)

        val props = Properties().apply {
            setProperty("FILE_NAME", kebab)
            setProperty("CLASS_NAME", className)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val srcDir = libRoot.findChild("src")!!
        val libDir = srcDir.findChild("lib")!!
        val serviceDir = libDir.createChildDirectory(this, kebab)

        val serviceTpl = templateManager.getInternalTemplate("Data Access Service")
        createFile(serviceDir, "$kebab.service.ts", serviceTpl.getText(props))

        val specTpl = templateManager.getInternalTemplate("Data Access Service Spec")
        createFile(serviceDir, "$kebab.service.spec.ts", specTpl.getText(props))

        overwriteFile(srcDir, "index.ts", "export { ${className}Service } from './lib/$kebab/$kebab.service'\n")

        return serviceDir.findChild("$kebab.service.ts")!!
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
