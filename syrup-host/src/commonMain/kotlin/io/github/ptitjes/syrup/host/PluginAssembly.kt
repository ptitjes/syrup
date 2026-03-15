package io.github.ptitjes.syrup.host

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.internal.DefaultPluginAssembly
import io.github.ptitjes.syrup.host.internal.DefaultPluginManager
import io.github.ptitjes.syrup.host.internal.NoopLogger
import org.kodein.di.DI

fun assemblePlugins(
    logger: Logger = NoopLogger,
    block: PluginAssembly.() -> Unit = { loadPlugins() }
): PluginManager {
    val assembly = DefaultPluginAssembly(logger = logger)
    block(assembly)
    return DefaultPluginManager(
        plugins = assembly.plugins,
        internalPluginBindings = { plugin ->
            assembly.additionalPluginBindings.forEach { contribution ->
                contribution(this, plugin)
            }
        },
        logger = logger,
    )
}

interface PluginAssembly {
    val plugins: Set<Plugin>
    fun loadPlugins()
    fun filterPlugins(predicate: (Plugin) -> Boolean)

    fun contributePluginBindings(builder: DI.Builder.(Plugin) -> Unit)
}
