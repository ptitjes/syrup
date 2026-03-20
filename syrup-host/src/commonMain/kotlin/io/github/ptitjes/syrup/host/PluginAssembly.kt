package io.github.ptitjes.syrup.host

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.internal.DefaultPluginAssembly
import io.github.ptitjes.syrup.host.internal.DefaultPluginManager
import io.github.ptitjes.syrup.host.internal.NoopLogger
import org.kodein.di.DI

public fun assemblePlugins(
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

public interface PluginAssembly {
    public val plugins: Set<Plugin>
    public fun loadPlugins()
    public fun filterPlugins(predicate: (Plugin) -> Boolean)
    public fun contributePluginBindings(builder: DI.Builder.(Plugin) -> Unit)
}
