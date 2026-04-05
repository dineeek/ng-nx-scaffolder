package com.ngscaffolder.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.APP)
@State(
    name = "com.ngscaffolder.settings.PluginSettings",
    storages = [Storage("NgNxScaffolderSettings.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettingsState> {

    private val myState = PluginSettingsState()

    override fun getState(): PluginSettingsState = myState

    override fun loadState(state: PluginSettingsState) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    companion object {
        fun getInstance(): PluginSettings =
            ApplicationManager.getApplication().getService(PluginSettings::class.java)
    }
}
