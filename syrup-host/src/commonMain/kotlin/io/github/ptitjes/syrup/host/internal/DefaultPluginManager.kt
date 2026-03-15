package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.host.Logger
import io.github.ptitjes.syrup.host.PluginManager
import io.github.ptitjes.syrup.specification.PluginSpecification
import org.kodein.di.DI

internal class DefaultPluginManager(
    private val plugins: Set<Plugin>,
    private val internalPluginBindings: DI.Builder.(Plugin) -> Unit = {},
    private val logger: Logger = NoopLogger,
) : PluginManager {

    fun internalDiFor(plugin: Plugin): DI = scaffolds.getValue(plugin).internalDi

    private val specifications: Map<Plugin, PluginSpecification>
    private val scaffolds: Map<Plugin, PluginScaffold>
    override val mainDi: DI

    init {
        specifications = buildPluginSpecifications()
        validatePluginSpecifications()

        scaffolds = buildPluginScaffolds()
        mainDi = buildMainDi()
    }

    private fun buildPluginSpecifications(): Map<Plugin, PluginSpecification> {
        logger.info { "Building plugin specifications" }

        return plugins.associateWith { plugin ->
            PluginSpecification { plugin.apply { specification() } }
        }
    }

    private fun validatePluginSpecifications() {
        logger.info { "Validating plugins" }

        PluginValidator(plugins, specifications).validate()
    }

    private fun buildPluginScaffolds(): MutableMap<Plugin, PluginScaffold> {
        logger.info { "Scaffolding plugins" }

        val sortedPlugins by lazy { plugins.topologicalSort() }
        val perPluginDependents = mutableMapOf<Plugin, MutableSet<Plugin>>()
        val scaffolds = mutableMapOf<Plugin, PluginScaffold>()

        sortedPlugins.forEach { plugin ->
            logger.info { "Building DI for plugin ${plugin.id}" }
            plugin.dependencies.forEach { dependency ->
                perPluginDependents.getOrPut(dependency) { mutableSetOf() }.add(plugin)
            }

            scaffolds[plugin] = PluginScaffold(
                plugin = plugin,
                specification = specifications.getValue(plugin),
                internalBindings = {
                    import(DI.Module("framework") { internalPluginBindings(plugin) })
                    plugin.apply { implementation() }
                },
                logger = logger,
            )
        }

        sortedPlugins.forEach { plugin ->
            val dependencies = plugin.dependencies.map { scaffolds.getValue(it) }
            val dependents = perPluginDependents[plugin]?.map { scaffolds.getValue(it) }
                ?: emptyList()

            scaffolds.getValue(plugin).wire(dependencies, dependents)
        }

        return scaffolds
    }

    private fun buildMainDi(): DI {
        logger.debug { "Building main DI" }

        return ModularDI(
            debugName = "main",
            sourceDi = DI {},
            callbackDi = null,
            bindingSourceProvider = {
                scaffolds.values.map { it.exposedTypes }.flattenBindingSources()
            },
            logger = logger,
        )
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
