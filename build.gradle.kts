plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.ngscaffolder"
version = "1.0.0-beta.1"

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
            <p>Right-click any folder inside <code>libs/</code> → <b>New</b> → <b>ng-nx-scaffolder</b>:</p>

            <h4>Feature Library</h4>
            <p>Container component, store (signalStore), facade, form service, mapper, routing</p>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/feature/create.png" alt="Feature Library Dialog" width="400"/>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/feature/preview.png" alt="Feature Library Preview" width="500"/>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/feature/generated.png" alt="Feature Library Generated" width="700"/>

            <h4>Data Access Library</h4>
            <p>HttpClient service with spec</p>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/data-access/create.png" alt="Data Access Library Dialog" width="400"/>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/data-access/generated.png" alt="Data Access Library Generated" width="700"/>

            <h4>UI Library</h4>
            <p>Standalone component with spec, HTML, SCSS</p>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/ui/create.png" alt="UI Library Dialog" width="400"/>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/ui/generated.png" alt="UI Library Generated" width="700"/>

            <h4>Model Library</h4>
            <p>TypeScript interface/model file</p>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/model/create.png" alt="Model Library Dialog" width="400"/>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/model/generated.png" alt="Model Library Generated" width="700"/>

            <h4>Util Library</h4>
            <p>Utility function with spec</p>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/util/create.png" alt="Util Library Dialog" width="400"/>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/util/generated.png" alt="Util Library Generated" width="700"/>

            <h3>Features</h3>
            <ul>
                <li>Tree preview of all files before generation</li>
                <li>Automatic Nx workspace detection</li>
                <li>Optional type suffix for library names (-feature, -data-access, -model, -ui, -util)</li>
                <li>Publishable library support (ng-package.json, package.json)</li>
                <li>Flattens Nx 18+ nested directory output automatically</li>
                <li>Fixes tsconfig path aliases and project names after generation</li>
                <li>Preserves workspace files (.prettierignore, nx.json) from Nx side effects</li>
                <li>Duplicate library name detection</li>
                <li>Configurable selector prefix and Nx generator in Settings → Tools → Angular/Nx Scaffolder</li>
            </ul>

            <h3>How to Use</h3>
            <img src="https://raw.githubusercontent.com/dineeek/ng-nx-scaffolder/main/assets/screenshots/menu/menu-actions.png" alt="Context Menu" width="700"/>
            <ol>
                <li>Right-click on a folder inside <code>libs/</code> (or any subfolder)</li>
                <li>Select <b>New → ng-nx-scaffolder → [Library Type]</b></li>
                <li>Enter the library name (kebab-case)</li>
                <li>Review the file preview and click <b>OK</b></li>
            </ol>

            <h3>Requirements</h3>
            <ul>
                <li><b>Angular 17+</b> — generated code uses standalone components, <code>inject()</code>, and <code>@ngrx/signals</code> signalStore</li>
                <li><b>Nx 16+</b> workspace with <code>@nx/angular</code> installed</li>
                <li>Node.js installed and accessible</li>
                <li>IntelliJ IDEA / WebStorm 2024.1+</li>
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
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
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
