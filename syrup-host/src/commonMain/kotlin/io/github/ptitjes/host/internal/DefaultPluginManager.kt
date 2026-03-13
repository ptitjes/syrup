package io.github.ptitjes.host.internal

import dev.whyoleg.sweetspi.ServiceLoader
import io.github.ptitjes.host.PluginManager
import io.github.ptitjes.syrup.Plugin
import org.kodein.di.DI

internal class DefaultPluginManager(
    private val pluginFilter: (Plugin) -> Boolean,
    private val internalPluginBindings: (Plugin) -> DI.Module?
) : PluginManager {

    override fun diFor(plugin: Plugin): DI = pluginHolderFor(plugin).di

    private val perPluginHolder = mutableMapOf<Plugin, PluginHolder>()
    private fun pluginHolderFor(plugin: Plugin): PluginHolder =
        perPluginHolder[plugin] ?: error("Plugin $plugin not bootstrapped")

    init {
        load()
    }

    private fun load() {
        val load = ServiceLoader.load<Plugin>()
        val plugins = load.filter(pluginFilter)

        // TODO check for circular plugin dependencies

        val sortedPlugins by lazy { plugins.topologicalSort() }
        val perPluginDependents = mutableMapOf<Plugin, MutableSet<Plugin>>()

        debug("Bootstrapping")

        sortedPlugins.forEach { plugin ->
            val pluginName = plugin::class.simpleName!!
            val dependencyHolders = plugin.dependencies
                .mapTo(mutableSetOf()) { pluginHolderFor(it) }

            PluginHolder(
                pluginName = pluginName,
                dependencies = dependencyHolders,
                api = {
                    plugin.apply { api() }
                },
                implementation = {
                    internalPluginBindings(plugin)?.let { import(it.rename("contributed")) }
                    plugin.apply { implementation() }
                },
            ).also { holder -> perPluginHolder[plugin] = holder }

            plugin.dependencies.forEach { dependency ->
                perPluginDependents.getOrPut(dependency) { mutableSetOf() }.add(plugin)
            }
        }

        sortedPlugins.forEach { plugin ->
            val dependents = perPluginDependents[plugin] ?: emptySet()
            val dependentHolders = dependents
                .mapTo(mutableSetOf()) { perPluginHolder.getValue(it) }

            perPluginHolder.getValue(plugin).setDependents(dependentHolders)
        }
    }

    private class PluginHolder(
        val pluginName: String,
        val dependencies: Set<PluginHolder>,
        api: DI.Builder.() -> Unit = {},
        implementation: DI.Builder.() -> Unit = {},
    ) {
        private lateinit var dependents: Set<PluginHolder>

        fun setDependents(dependents: Set<PluginHolder>) {
            this.dependents = dependents
        }

        private val upwardSources by lazy {
            listOf(this.exposedSetBindings) + dependents.map { dependent ->
                dependent.exposedSetBindings
            }
        }

        private val downwardSources by lazy {
            dependencies.map { dependency ->
                dependency.exposedBindings
            }
        }

        val sources by lazy {
            (upwardSources + downwardSources).flattenBindingSources()
        }

        val pluginDIs = PluginDIs(
            name = pluginName,
            api = api,
            implementation = implementation,
            bindingSourceProvider = { sources },
        )

        val di: DI = pluginDIs.publicDi

        val exposedSetBindings: BindingSource
            get() = di.asSource("$pluginName-upward").onlyIf { key -> key.isSetKey() }

        val exposedBindings: BindingSource
            get() = di.asSource("$pluginName-downward").onlyIf { key -> !key.isSetKey() }
    }
}

private fun Iterable<Plugin>.topologicalSort(): List<Plugin> {
    val sorted = mutableListOf<Plugin>()
    val visited = mutableSetOf<Plugin>()

    fun visit(plugin: Plugin) {
        if (plugin in visited) return
        visited += plugin
        plugin.dependencies.forEach { visit(it) }
        sorted += plugin
    }

    forEach { visit(it) }

    return sorted
}
