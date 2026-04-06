package com.ngscaffolder.settings

data class PluginSettingsState(
    var selectorPrefix: String = "app",
    var nxGenerator: String = "@nx/angular:library",
    var playwrightFixtureType: String = "Fixtures",
    var playwrightDomainPrefix: String = "",
)
