package com.ngscaffolder.settings

data class PluginSettingsState(
    var selectorPrefix: String = "app",
    var generateSpec: Boolean = true,
    var generateScss: Boolean = true,
    var generateHtml: Boolean = true,
    var includeRxMethod: Boolean = true,
    var dataAccessBaseUrlPattern: String = "api",
    var dataAccessMethodSuffix: String = "",
    var playwrightFixtureType: String = "Fixtures",
    var playwrightDomainPrefix: String = "",
    var nxDefaultDomain: String = "",
    var nxDefaultType: String = "feature",
)
