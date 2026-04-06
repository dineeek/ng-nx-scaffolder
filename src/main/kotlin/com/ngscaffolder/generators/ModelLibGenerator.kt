package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

class ModelLibGenerator(private val project: Project) {

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
        val modelsDir = libDir.createChildDirectory(this, "models")

        // Config files
        ConfigFileGenerator(project).generate(
            directory = directory,
            srcDir = srcDir,
            libName = kebab,
            prefix = kebab,
            libType = "model",
            hasSpecs = false,
            hasNgPackage = false,
            hasStyles = false,
        )

        val tpl = templateManager.getInternalTemplate("Model Interface")
        createFile(modelsDir, "$kebab.model.ts", tpl.getText(props))

        createFile(srcDir, "index.ts", "export { I$className } from './lib/models/$kebab.model'\n")

        return modelsDir.findChild("$kebab.model.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
