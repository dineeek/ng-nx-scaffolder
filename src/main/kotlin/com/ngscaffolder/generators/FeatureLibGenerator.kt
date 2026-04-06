package com.ngscaffolder.generators

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ngscaffolder.util.NamingUtils
import java.util.*

data class FeatureLibOptions(
    val name: String,
    val prefix: String,
    val hasStore: Boolean,
    val hasFacade: Boolean,
    val hasForm: Boolean,
    val hasRouting: Boolean,
    val isDialog: Boolean,
)

class FeatureLibGenerator(private val project: Project) {

    fun generate(directory: VirtualFile, options: FeatureLibOptions): VirtualFile {
        val kebab = NamingUtils.toKebabCase(options.name)
        val className = NamingUtils.toPascalCase(options.name)

        val componentName = "$kebab-container"
        val componentClass = "${className}ContainerComponent"

        val storeName = kebab
        val storeClass = "${className}Store"
        val storeStateName = "I${className}State"

        val facadeName = "$kebab-facade"
        val facadeClass = "${className}FacadeService"

        val formName = "$kebab-form"
        val formInterface = "I${className}Form"
        val formClass = "${className}FormService"

        val mapperName = kebab
        val mapperClass = "${className}Mapper"

        val routesConst = "${className.uppercase()}_ROUTES"

        val dialogModelName = "$kebab-dialog"
        val dialogDataInterface = "I${className}DialogData"
        val dialogResponseInterface = "I${className}DialogResponse"

        val props = Properties().apply {
            setProperty("PREFIX", options.prefix)
            setProperty("COMPONENT_NAME", componentName)
            setProperty("COMPONENT_CLASS", componentClass)
            setProperty("STORE_NAME", storeName)
            setProperty("STORE_CLASS", storeClass)
            setProperty("STORE_STATE_NAME", storeStateName)
            setProperty("FACADE_NAME", facadeName)
            setProperty("FACADE_CLASS", facadeClass)
            setProperty("FORM_NAME", formName)
            setProperty("FORM_INTERFACE", formInterface)
            setProperty("FORM_CLASS", formClass)
            setProperty("MAPPER_NAME", mapperName)
            setProperty("MAPPER_CLASS", mapperClass)
            setProperty("ROUTES_CONST", routesConst)
            setProperty("DIALOG_MODEL_NAME", dialogModelName)
            setProperty("DIALOG_DATA_INTERFACE", dialogDataInterface)
            setProperty("DIALOG_RESPONSE_INTERFACE", dialogResponseInterface)
            setProperty("HAS_STORE", options.hasStore.toString())
            setProperty("HAS_FACADE", options.hasFacade.toString())
            setProperty("HAS_FORM", options.hasForm.toString())
        }

        val templateManager = FileTemplateManager.getInstance(project)

        // Create directory structure
        val srcDir = directory.createChildDirectory(this, "src")
        val libDir = srcDir.createChildDirectory(this, "lib")

        // Config files
        ConfigFileGenerator(project).generate(
            directory = directory,
            srcDir = srcDir,
            libName = kebab,
            prefix = options.prefix,
            libType = "feature",
            hasSpecs = true,
            hasNgPackage = true,
            hasStyles = true,
        )

        // Container
        val containerDir = libDir.createChildDirectory(this, "container")
        if (options.isDialog) {
            val tpl = templateManager.getInternalTemplate("Feature Container Dialog Component")
            createFile(containerDir, "$componentName.component.ts", tpl.getText(props))
        } else {
            val tpl = templateManager.getInternalTemplate("Feature Container Component")
            createFile(containerDir, "$componentName.component.ts", tpl.getText(props))
        }
        val htmlTpl = templateManager.getInternalTemplate("Feature Container Component HTML")
        createFile(containerDir, "$componentName.component.html", htmlTpl.getText(props))
        createFile(containerDir, "$componentName.component.scss", "")
        val specTpl = templateManager.getInternalTemplate("Feature Container Component Spec")
        createFile(containerDir, "$componentName.component.spec.ts", specTpl.getText(props))

        // Store (conditional)
        if (options.hasStore) {
            val storeDir = libDir.createChildDirectory(this, "store")
            val storeTpl = templateManager.getInternalTemplate("Feature Store")
            createFile(storeDir, "$storeName.store.ts", storeTpl.getText(props))
            val stateTpl = templateManager.getInternalTemplate("Feature Store State")
            createFile(storeDir, "$storeName.state.ts", stateTpl.getText(props))
            val storeSpecTpl = templateManager.getInternalTemplate("Feature Store Spec")
            createFile(storeDir, "$storeName.store.spec.ts", storeSpecTpl.getText(props))
        }

        // Facade (conditional)
        if (options.hasFacade) {
            val facadeDir = libDir.createChildDirectory(this, "facade")
            val facadeTpl = templateManager.getInternalTemplate("Feature Facade")
            createFile(facadeDir, "$facadeName.service.ts", facadeTpl.getText(props))
            val facadeSpecTpl = templateManager.getInternalTemplate("Feature Facade Spec")
            createFile(facadeDir, "$facadeName.service.spec.ts", facadeSpecTpl.getText(props))
        }

        // Form (conditional)
        if (options.hasForm) {
            val formDir = libDir.createChildDirectory(this, "form")
            val formTpl = templateManager.getInternalTemplate("Feature Form Service")
            createFile(formDir, "$formName.service.ts", formTpl.getText(props))
            val formModelTpl = templateManager.getInternalTemplate("Feature Form Model")
            createFile(formDir, "$formName.model.ts", formModelTpl.getText(props))
            val formSpecTpl = templateManager.getInternalTemplate("Feature Form Spec")
            createFile(formDir, "$formName.service.spec.ts", formSpecTpl.getText(props))
        }

        // Mapper (always)
        val mapperDir = libDir.createChildDirectory(this, "mapper")
        val mapperTpl = templateManager.getInternalTemplate("Feature Mapper")
        createFile(mapperDir, "$mapperName.mapper.ts", mapperTpl.getText(props))
        val mapperSpecTpl = templateManager.getInternalTemplate("Feature Mapper Spec")
        createFile(mapperDir, "$mapperName.mapper.spec.ts", mapperSpecTpl.getText(props))

        // Models
        val modelsDir = libDir.createChildDirectory(this, "models")
        val exampleModelTpl = templateManager.getInternalTemplate("Feature Example Model")
        createFile(modelsDir, "example.model.ts", exampleModelTpl.getText(props))

        // Dialog model (conditional)
        if (options.isDialog) {
            val dialogModelTpl = templateManager.getInternalTemplate("Feature Dialog Model")
            createFile(modelsDir, "$dialogModelName.model.ts", dialogModelTpl.getText(props))
        }

        // Routing (conditional)
        if (options.hasRouting && !options.isDialog) {
            val routesTpl = templateManager.getInternalTemplate("Feature Routes")
            createFile(libDir, "${kebab}.routes.ts", routesTpl.getText(props))
        }

        // Barrel export
        val barrelLines = mutableListOf("export * from './lib/container/$componentName.component'")
        if (options.hasRouting && !options.isDialog) {
            barrelLines.add("export * from './lib/${kebab}.routes'")
        }
        if (options.isDialog) {
            barrelLines.add("export * from './lib/models/$dialogModelName.model'")
        }
        createFile(srcDir, "index.ts", barrelLines.joinToString("\n") + "\n")

        return containerDir.findChild("$componentName.component.ts")!!
    }

    private fun createFile(dir: VirtualFile, fileName: String, content: String) {
        val file = dir.createChildData(this, fileName)
        file.setBinaryContent(content.toByteArray())
    }
}
