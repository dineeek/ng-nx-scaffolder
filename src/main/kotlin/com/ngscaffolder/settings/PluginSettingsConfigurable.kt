package com.ngscaffolder.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class PluginSettingsConfigurable : BoundConfigurable("Angular/Nx Scaffolder") {

    override fun createPanel(): DialogPanel {
        val settings = PluginSettings.getInstance().state
        return panel {
            group("Nx Library Defaults") {
                row("Selector prefix:") {
                    textField()
                        .bindText(settings::selectorPrefix)
                        .comment("e.g. app, sp-feature, ef")
                }
                row("Default domain:") {
                    textField()
                        .bindText(settings::nxDefaultDomain)
                        .comment("e.g. sales, ffu, hub")
                }
            }
            group("Playwright E2E") {
                row("Fixture type:") {
                    textField()
                        .bindText(settings::playwrightFixtureType)
                }
                row("Domain prefix:") {
                    textField()
                        .bindText(settings::playwrightDomainPrefix)
                        .comment("e.g. SAL, FFU")
                }
            }
        }
    }
}
