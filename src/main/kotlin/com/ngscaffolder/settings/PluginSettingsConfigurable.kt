package com.ngscaffolder.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class PluginSettingsConfigurable : BoundConfigurable("Angular/Nx Scaffolder") {

    override fun createPanel(): DialogPanel {
        val settings = PluginSettings.getInstance().state
        return panel {
            group("General") {
                row("Selector prefix:") {
                    textField()
                        .bindText(settings::selectorPrefix)
                        .comment("Default component selector prefix")
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
                        .comment("Tag prefix used in test describe blocks")
                }
            }
        }
    }
}
