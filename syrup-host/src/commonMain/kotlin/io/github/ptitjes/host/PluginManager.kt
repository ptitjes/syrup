package io.github.ptitjes.host

import io.github.ptitjes.syrup.Plugin
import org.kodein.di.DI

interface PluginManager {
    fun diFor(plugin: Plugin): DI

    interface Factory {
        fun create(
            plugins: Set<Plugin>,
            internalPluginBindings: (Plugin) -> DI.Module? = { null },
        ): PluginManager
    }
}
