plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
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
        intellijIdeaUltimate("2024.1.7")
        bundledPlugin("JavaScript")
        instrumentationTools()
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            Code generation plugin for Angular and Nx developers.
            <ul>
                <li>Angular standalone components (signals, inject, OnPush)</li>
                <li>NgRx SignalStore (withState, rxMethod, patchState)</li>
                <li>Data-access services (HttpClient, Observable CRUD)</li>
                <li>Playwright E2E tests (POM, locators, test steps)</li>
                <li>Nx library structure scaffolding</li>
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

tasks {
    test {
        useJUnitPlatform()
    }
}
