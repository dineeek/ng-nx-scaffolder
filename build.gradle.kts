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

detekt {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = true
}

tasks {
    test {
        useJUnitPlatform()
    }
}
