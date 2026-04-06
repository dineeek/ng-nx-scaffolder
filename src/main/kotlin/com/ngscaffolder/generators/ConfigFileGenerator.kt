package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.*

class ConfigFileGenerator(private val project: Project) {

    fun generate(
        directory: VirtualFile,
        srcDir: VirtualFile,
        libName: String,
        prefix: String,
        libType: String,
        hasSpecs: Boolean,
        hasNgPackage: Boolean,
        hasStyles: Boolean,
    ) {
        val props = Properties().apply {
            setProperty("LIB_NAME", libName)
            setProperty("PREFIX", prefix)
            setProperty("LIB_TYPE", libType)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        // tsconfig.json (with or without spec reference)
        val tsconfigTemplate = if (hasSpecs) "Lib tsconfig" else "Lib tsconfig no spec"
        createFile(directory, "tsconfig.json", templateManager.getInternalTemplate(tsconfigTemplate).getText(props))

        // tsconfig.lib.json (always)
        createFile(directory, "tsconfig.lib.json", templateManager.getInternalTemplate("Lib tsconfig.lib").getText(props))

        // tsconfig.lib.prod.json (always)
        createFile(directory, "tsconfig.lib.prod.json", templateManager.getInternalTemplate("Lib tsconfig.lib.prod").getText(props))

        // tsconfig.eslint.json (always)
        createFile(directory, "tsconfig.eslint.json", templateManager.getInternalTemplate("Lib tsconfig.eslint").getText(props))

        // .eslintrc.json (always)
        createFile(directory, ".eslintrc.json", templateManager.getInternalTemplate("Lib eslintrc").getText(props))

        // project.json (always)
        createFile(directory, "project.json", templateManager.getInternalTemplate("Lib project").getText(props))

        // package.json (always)
        createFile(directory, "package.json", templateManager.getInternalTemplate("Lib package").getText(props))

        // README.md (always)
        createFile(directory, "README.md", templateManager.getInternalTemplate("Lib README").getText(props))

        // tsconfig.spec.json + jest.config.ts + test-setup.ts (conditional)
        if (hasSpecs) {
            createFile(directory, "tsconfig.spec.json", templateManager.getInternalTemplate("Lib tsconfig.spec").getText(props))
            createFile(directory, "jest.config.ts", templateManager.getInternalTemplate("Lib jest.config").getText(props))
            createFile(srcDir, "test-setup.ts", templateManager.getInternalTemplate("Lib test-setup").getText(props))
        }

        // ng-package.json (conditional)
        if (hasNgPackage) {
            createFile(directory, "ng-package.json", templateManager.getInternalTemplate("Lib ng-package").getText(props))
        }

        // .stylelintrc.json (conditional)
        if (hasStyles) {
            createFile(directory, ".stylelintrc.json", templateManager.getInternalTemplate("Lib stylelintrc").getText(props))
        }
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
