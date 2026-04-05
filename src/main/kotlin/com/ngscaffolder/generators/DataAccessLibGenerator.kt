package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

class DataAccessLibGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, name: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)
        val className = NamingUtils.toPascalCase(name)

        val props = Properties().apply {
            setProperty("FILE_NAME", kebab)
            setProperty("CLASS_NAME", className)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val srcDir = directory.createChildDirectory(this, "src")
        val libDir = srcDir.createChildDirectory(this, "lib")
        val servicesDir = libDir.createChildDirectory(this, "services")

        val serviceTpl = templateManager.getInternalTemplate("Data Access Service")
        createFile(servicesDir, "$kebab.service.ts", serviceTpl.getText(props))

        val specTpl = templateManager.getInternalTemplate("Data Access Service Spec")
        createFile(servicesDir, "$kebab.service.spec.ts", specTpl.getText(props))

        createFile(srcDir, "index.ts", "export { ${className}Service } from './lib/services/$kebab.service'\n")

        return servicesDir.findChild("$kebab.service.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
