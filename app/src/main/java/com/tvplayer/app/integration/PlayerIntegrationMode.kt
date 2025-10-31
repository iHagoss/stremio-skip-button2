package com.tvplayer.app.integration

sealed class PlayerIntegrationMode {
    object Internal : PlayerIntegrationMode()
    object ExternalHook : PlayerIntegrationMode()

    companion object {
        fun fromPreference(enabled: Boolean): PlayerIntegrationMode {
            return if (enabled) ExternalHook else Internal
        }
    }
}
