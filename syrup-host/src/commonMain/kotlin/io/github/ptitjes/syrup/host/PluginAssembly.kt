package io.github.ptitjes.syrup.host

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.internal.DefaultPluginAssembly
import io.github.ptitjes.syrup.host.internal.DefaultPluginManager
import io.github.ptitjes.syrup.host.internal.NoopLogger
import org.kodein.di.DI

/**
 * Entry point for assembling a [PluginManager] and configuring the plugins.
 *
 * Use the [block] to load or filter plugins, and contribute additional bindings.
 *
 * @param logger The logger to use for assembly and plugin management.
 * @param block The configuration block.
 */
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

/**
 * DSL for assembling and configuring plugins.
 */
public interface PluginAssembly {
    /**
     * The set of currently loaded plugins.
     */
    public val plugins: Set<Plugin>

    /**
     * Loads plugins using the [dev.whyoleg.sweetspi.ServiceLoader] mechanism.
     */
    public fun loadPlugins()

    /**
     * Filters the currently loaded plugins using the given [predicate].
     */
    public fun filterPlugins(predicate: (Plugin) -> Boolean)

    /**
     * Registers a [builder] to contribute additional bindings to each plugin's internal DI container.
     */
    public fun contributePluginBindings(builder: DI.Builder.(Plugin) -> Unit)
}
