package com.ngscaffolder.generators

import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils

class NxLibraryGenerator {

    fun generate(directory: VirtualFile, domain: String, type: String, name: String): VirtualFile {
        val kebab = NamingUtils.toKebabCase(name)
        val domainKebab = NamingUtils.toKebabCase(domain)
        val libName = "$type-$kebab"

        // libs/{domain}/{type}-{name}/src/lib/
        val domainDir = directory.findChild(domainKebab)
            ?: directory.createChildDirectory(this, domainKebab)
        val libDir = domainDir.createChildDirectory(this, libName)
        val srcDir = libDir.createChildDirectory(this, "src")
        val libSrcDir = srcDir.createChildDirectory(this, "lib")

        // Barrel export
        val indexContent = "export {};\n"
        val indexFile = srcDir.createChildData(this, "index.ts")
        indexFile.setBinaryContent(indexContent.toByteArray())

        // Inner index
        val libIndexFile = libSrcDir.createChildData(this, ".gitkeep")
        libIndexFile.setBinaryContent(ByteArray(0))

        return libDir
    }
}
