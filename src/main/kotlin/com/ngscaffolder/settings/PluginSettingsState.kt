package com.ngscaffolder.settings

data class PluginSettingsState(
    var selectorPrefix: String = "app",
    var playwrightFixtureType: String = "Fixtures",
    var playwrightDomainPrefix: String = "",
)
