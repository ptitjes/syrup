package io.github.ptitjes.host

import io.github.ptitjes.host.internal.DefaultPluginManager
import io.github.ptitjes.syrup.Plugin
import org.kodein.di.DI

interface PluginManager {
    fun diFor(plugin: Plugin): DI

    companion object {
        operator fun invoke(
            pluginFilter: (Plugin) -> Boolean = { true },
            internalPluginBindings: (Plugin) -> DI.Module? = { null },
        ): PluginManager {
            return DefaultPluginManager(
                pluginFilter = pluginFilter,
                internalPluginBindings = internalPluginBindings,
            )
        }
    }
}
