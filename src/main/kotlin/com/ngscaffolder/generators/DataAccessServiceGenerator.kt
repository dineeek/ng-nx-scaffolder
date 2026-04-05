package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.settings.PluginSettings
import com.ngscaffolder.util.NamingUtils
import java.util.*

class DataAccessServiceGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, name: String, entityName: String): VirtualFile {
        val settings = PluginSettings.getInstance().state
        val kebab = NamingUtils.toKebabCase(name)
        val pascal = NamingUtils.toPascalCase(name)
        val entityPascal = NamingUtils.toPascalCase(entityName)
        val suffix = settings.dataAccessMethodSuffix

        val props = Properties().apply {
            setProperty("NAME_KEBAB", kebab)
            setProperty("NAME_PASCAL", pascal)
            setProperty("ENTITY_PASCAL", entityPascal)
            setProperty("METHOD_SUFFIX", suffix)
            setProperty("BASE_URL", settings.dataAccessBaseUrlPattern)
        }

        val templateManager = FileTemplateManager.getInstance(project)
        val template = templateManager.getInternalTemplate("Data Access Service")
        createFile(directory, "$kebab.service.ts", template.getText(props))

        return directory.findChild("$kebab.service.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
