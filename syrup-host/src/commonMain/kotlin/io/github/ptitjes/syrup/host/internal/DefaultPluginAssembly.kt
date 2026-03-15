package io.github.ptitjes.syrup.host.internal

import dev.whyoleg.sweetspi.ServiceLoader
import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.Logger
import io.github.ptitjes.syrup.host.PluginAssembly
import org.kodein.di.DI

internal class DefaultPluginAssembly(
    private val logger: Logger = NoopLogger,
) : PluginAssembly {
    override var plugins: Set<Plugin> = emptySet()
        private set

    override fun loadPlugins() {
        logger.debug { "Loading plugins" }
        plugins = ServiceLoader.load<Plugin>().toSet()
    }

    override fun filterPlugins(predicate: (Plugin) -> Boolean) {
        plugins = plugins.filter(predicate).toSet()
    }

    internal var additionalPluginBindings = listOf<DI.Builder.(Plugin) -> Unit>()
        private set

    override fun contributePluginBindings(builder: DI.Builder.(Plugin) -> Unit) {
        additionalPluginBindings += builder
    }
}
