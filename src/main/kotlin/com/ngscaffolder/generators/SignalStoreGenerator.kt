package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.settings.PluginSettings
import com.ngscaffolder.util.NamingUtils
import java.util.*

class SignalStoreGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, name: String): VirtualFile {
        val settings = PluginSettings.getInstance().state
        val kebab = NamingUtils.toKebabCase(name)
        val pascal = NamingUtils.toPascalCase(name)
        val camel = NamingUtils.toCamelCase(name)

        val props = Properties().apply {
            setProperty("NAME_KEBAB", kebab)
            setProperty("NAME_PASCAL", pascal)
            setProperty("NAME_CAMEL", camel)
            setProperty("INCLUDE_RX_METHOD", settings.includeRxMethod.toString())
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val storeTemplate = templateManager.getInternalTemplate("NgRx SignalStore")
        createFile(directory, "$kebab.store.ts", storeTemplate.getText(props))

        val stateTemplate = templateManager.getInternalTemplate("NgRx SignalStore State")
        createFile(directory, "$kebab.state.ts", stateTemplate.getText(props))

        return directory.findChild("$kebab.store.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
