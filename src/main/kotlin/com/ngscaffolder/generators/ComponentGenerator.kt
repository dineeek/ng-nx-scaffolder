package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.settings.PluginSettings
import com.ngscaffolder.util.NamingUtils
import java.util.*

class ComponentGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, name: String): VirtualFile {
        val settings = PluginSettings.getInstance().state
        val kebab = NamingUtils.toKebabCase(name)
        val pascal = NamingUtils.toPascalCase(name)
        val selector = "${settings.selectorPrefix}-$kebab"

        val props = Properties().apply {
            setProperty("NAME_KEBAB", kebab)
            setProperty("NAME_PASCAL", pascal)
            setProperty("SELECTOR", selector)
            setProperty("INLINE_TEMPLATE", (!settings.generateHtml).toString())
            setProperty("HAS_SCSS", settings.generateScss.toString())
        }

        val templateManager = FileTemplateManager.getInstance(project)
        val componentDir = directory.createChildDirectory(this, kebab)

        val componentTemplate = templateManager.getInternalTemplate("Angular Standalone Component")
        createFile(componentDir, "$kebab.component.ts", componentTemplate.getText(props))

        if (settings.generateHtml) {
            val htmlTemplate = templateManager.getInternalTemplate("Angular Component HTML")
            createFile(componentDir, "$kebab.component.html", htmlTemplate.getText(props))
        }

        if (settings.generateScss) {
            createFile(componentDir, "$kebab.component.scss", "")
        }

        if (settings.generateSpec) {
            val specTemplate = templateManager.getInternalTemplate("Angular Component Spec")
            createFile(componentDir, "$kebab.component.spec.ts", specTemplate.getText(props))
        }

        return componentDir.findChild("$kebab.component.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
