plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.ngscaffolder"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1.7")
        instrumentationTools()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.ngscaffolder.ng-nx-scaffolder"
        name = "Angular/Nx Scaffolder"
        version = project.version.toString()
        description = """
            <h2>Angular/Nx Scaffolder</h2>
            <p>
                Stop burning AI tokens on boilerplate. This plugin generates production-ready Angular/Nx library scaffolding
                in seconds — with correct structure, test setup, and barrel exports — directly from the IDE context menu.
            </p>

            <h3>Library Generators</h3>
            <p>Right-click any folder inside <code>libs/</code> → <b>New</b> → <b>Angular/Nx</b>:</p>
            <ul>
                <li><b>Feature Library</b> — container component, store (signalStore), facade, form service, mapper, routing</li>
                <li><b>Data-Access Library</b> — HttpClient service with spec</li>
                <li><b>UI Library</b> — standalone component with spec, HTML, SCSS</li>
                <li><b>Model Library</b> — TypeScript interface/model file</li>
                <li><b>Util Library</b> — utility function with spec</li>
            </ul>

            <h3>Features</h3>
            <ul>
                <li>Tree preview of all files before generation</li>
                <li>Automatic Nx workspace detection</li>
                <li>Publishable library support (ng-package.json, package.json)</li>
                <li>Flattens Nx 18+ nested directory output automatically</li>
                <li>Fixes tsconfig path aliases and project names after generation</li>
                <li>Preserves workspace files (.prettierignore, nx.json) from Nx side effects</li>
                <li>Duplicate library name detection</li>
                <li>Configurable selector prefix and Nx generator in Settings → Tools → Angular/Nx Scaffolder</li>
            </ul>

            <h3>How to Use</h3>
            <ol>
                <li>Right-click on a folder inside <code>libs/</code> (or any subfolder)</li>
                <li>Select <b>New → Angular/Nx → [Library Type]</b></li>
                <li>Enter the library name (kebab-case)</li>
                <li>Review the file preview and click <b>Generate</b></li>
            </ol>

            <h3>Requirements</h3>
            <ul>
                <li>Nx workspace with <code>nx.json</code> at the root</li>
                <li>Node.js installed and accessible</li>
                <li>IntelliJ IDEA / WebStorm 2023.1+</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
        vendor {
            name = "dineeek"
            url = "https://github.com/dineeek/ng-nx-scaffolder"
        }
    }
}

detekt {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = true
}

tasks {
    test {
        useJUnitPlatform()
    }
}
