package io.github.ptitjes.host.internal

import dev.whyoleg.sweetspi.ServiceLoader
import io.github.ptitjes.host.PluginAssembly
import io.github.ptitjes.syrup.Plugin
import org.kodein.di.DI

internal class DefaultPluginAssembly : PluginAssembly {
    override var plugins: Set<Plugin> = emptySet()
        private set

    override fun loadPlugins() {
        debug("Loading plugins")
        plugins = ServiceLoader.load<Plugin>().toSet()
    }

    override fun filterPlugins(predicate: (Plugin) -> Boolean) {
        plugins = plugins.filter(predicate).toSet()
    }

    internal var pluginContributions = listOf<DI.Builder.(Plugin) -> Unit>()
        private set

    override fun contributePluginBindings(builder: DI.Builder.(Plugin) -> Unit) {
        pluginContributions += builder
    }
}
