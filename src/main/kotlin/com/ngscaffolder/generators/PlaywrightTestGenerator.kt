package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.settings.PluginSettings
import com.ngscaffolder.util.NamingUtils
import java.util.*

class PlaywrightTestGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, featureName: String, testCaseName: String): VirtualFile {
        val settings = PluginSettings.getInstance().state
        val featureKebab = NamingUtils.toKebabCase(featureName)
        val testKebab = NamingUtils.toKebabCase(testCaseName)
        val testPascal = NamingUtils.toPascalCase(testCaseName)
        val testCamel = NamingUtils.toCamelCase(testCaseName)
        val featurePascal = NamingUtils.toPascalCase(featureName)

        val props = Properties().apply {
            setProperty("FEATURE_KEBAB", featureKebab)
            setProperty("FEATURE_PASCAL", featurePascal)
            setProperty("TEST_KEBAB", testKebab)
            setProperty("TEST_PASCAL", testPascal)
            setProperty("TEST_CAMEL", testCamel)
            setProperty("FIXTURE_TYPE", settings.playwrightFixtureType)
            setProperty("DOMAIN_PREFIX", settings.playwrightDomainPrefix)
        }

        val templateManager = FileTemplateManager.getInstance(project)

        // Create feature folder structure
        val featureDir = directory.createChildDirectory(this, "feature-$featureKebab")
        val testsDir = featureDir.createChildDirectory(this, "tests")
        val cyclesDir = featureDir.createChildDirectory(this, "cycles")

        // Test file
        val testTemplate = templateManager.getInternalTemplate("Playwright Test")
        createFile(testsDir, "$testKebab.ts", testTemplate.getText(props))

        // Index
        createFile(testsDir, "index.ts", "export { $testCamel } from './$testKebab';\n")

        // Cycle spec
        val cycleTemplate = templateManager.getInternalTemplate("Playwright Cycle")
        createFile(cyclesDir, "all.spec.ts", cycleTemplate.getText(props))

        // POM files
        val pagesDir = featureDir.createChildDirectory(this, "pages")

        val locatorsTemplate = templateManager.getInternalTemplate("Playwright Locators")
        createFile(pagesDir, "$featureKebab.locators.ts", locatorsTemplate.getText(props))

        val pageTemplate = templateManager.getInternalTemplate("Playwright Page")
        createFile(pagesDir, "$featureKebab.page.ts", pageTemplate.getText(props))

        createFile(pagesDir, "index.ts",
            "export { ${featurePascal}Page } from './$featureKebab.page';\nexport { ${featurePascal.uppercase()}_IDS, LOCATORS } from './$featureKebab.locators';\n"
        )

        return featureDir
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
