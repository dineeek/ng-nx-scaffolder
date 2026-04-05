package com.ngscaffolder.settings

data class PluginSettingsState(
    var selectorPrefix: String = "app",
    var nxDefaultDomain: String = "",
    var playwrightFixtureType: String = "Fixtures",
    var playwrightDomainPrefix: String = "",
)
