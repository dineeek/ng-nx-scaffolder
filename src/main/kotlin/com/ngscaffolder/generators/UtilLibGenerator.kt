package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

class UtilLibGenerator(private val project: Project) {

    fun generate(libRoot: VirtualFile, name: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)

        val props = Properties().apply {
            setProperty("FILE_NAME", kebab)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        val srcDir = libRoot.findChild("src")!!
        val libDir = srcDir.findChild("lib")!!
        val utilDir = libDir.createChildDirectory(this, kebab)

        val utilTpl = templateManager.getInternalTemplate("Util File")
        createFile(utilDir, "$kebab.util.ts", utilTpl.getText(props))

        val specTpl = templateManager.getInternalTemplate("Util Spec")
        createFile(utilDir, "$kebab.util.spec.ts", specTpl.getText(props))

        overwriteFile(srcDir, "index.ts", "export {} from './lib/$kebab/$kebab.util'\n")

        return utilDir.findChild("$kebab.util.ts")!!
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
