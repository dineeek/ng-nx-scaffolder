package com.ngscaffolder.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class PluginSettingsConfigurable : BoundConfigurable("Angular/Nx Scaffolder") {

    override fun createPanel(): DialogPanel {
        val settings = PluginSettings.getInstance().state
        return panel {
            group("Angular Component") {
                row("Selector prefix:") {
                    textField()
                        .bindText(settings::selectorPrefix)
                        .comment("e.g. app, sp-feature, ef")
                }
                row {
                    checkBox("Generate .spec.ts")
                        .bindSelected(settings::generateSpec)
                }
                row {
                    checkBox("Generate .scss")
                        .bindSelected(settings::generateScss)
                }
                row {
                    checkBox("Generate .html (inline template if unchecked)")
                        .bindSelected(settings::generateHtml)
                }
            }
            group("NgRx SignalStore") {
                row {
                    checkBox("Include rxMethod boilerplate")
                        .bindSelected(settings::includeRxMethod)
                }
            }
            group("Data-Access Service") {
                row("Base URL pattern:") {
                    textField()
                        .bindText(settings::dataAccessBaseUrlPattern)
                }
                row("Method suffix:") {
                    textField()
                        .bindText(settings::dataAccessMethodSuffix)
                        .comment("e.g. \$ for Observable suffix convention")
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
            group("Nx Library") {
                row("Default domain:") {
                    textField()
                        .bindText(settings::nxDefaultDomain)
                }
                row("Default type:") {
                    textField()
                        .bindText(settings::nxDefaultType)
                        .comment("feature, ui, data-access, util, model")
                }
            }
        }
    }
}
