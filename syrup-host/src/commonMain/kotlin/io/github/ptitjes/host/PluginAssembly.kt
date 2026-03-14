package io.github.ptitjes.host

import io.github.ptitjes.host.internal.DefaultPluginAssembly
import io.github.ptitjes.host.internal.DefaultPluginManager
import io.github.ptitjes.syrup.Plugin
import org.kodein.di.DI

interface PluginAssembly {
    val plugins: Set<Plugin>
    fun loadPlugins()
    fun filterPlugins(predicate: (Plugin) -> Boolean)

    fun contributePluginBindings(builder: DI.Builder.(Plugin) -> Unit)
}

fun assemblePlugins(block: PluginAssembly.() -> Unit = { loadPlugins() }): PluginManager {
    val assembly = DefaultPluginAssembly()
    block(assembly)
    return DefaultPluginManager(
        plugins = assembly.plugins,
        internalPluginBindings = { plugin ->
            DI.Module {
                assembly.pluginContributions.forEach { contribution ->
                    contribution(this, plugin)
                }
            }
        }
    )
}
