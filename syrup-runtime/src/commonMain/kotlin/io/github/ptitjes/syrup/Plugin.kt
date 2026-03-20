package io.github.ptitjes.syrup

import dev.whyoleg.sweetspi.Service
import io.github.ptitjes.syrup.specification.PluginSpecificationBuilder
import org.kodein.di.DI

/**
 * Represents a plugin in the Syrup system.
 *
 * Plugins are the building blocks of a Syrup application. Each plugin can define
 * its own [specification] (API and extension points) and [implementation] (internal bindings).
 *
 * Plugins are discovered at runtime using a service loader mechanism.
 * They should be implemented as Kotlin objects and annotated with `@ServiceProvider`.
 */
@Service
public interface Plugin {
    /**
     * The unique identifier of this plugin.
     *
     * Defaults to the qualified name of the plugin class.
     */
    public val id: PluginId
        get() = PluginId(this::class.qualifiedName ?: error("Plugin class must have a qualified name"))

    /**
     * The set of plugins that this plugin depends on.
     */
    public val dependencies: Set<Plugin> get() = emptySet()

    /**
     * Defines the plugin's specification, including exposed types, extension points, and extension contributions.
     */
    public fun PluginSpecificationBuilder.specification() {}

    /**
     * Defines the plugin's internal bindings.
     *
     * These bindings are private to the plugin and cannot be accessed by other plugins,
     * unless they are explicitly exposed in the [specification].
     */
    public fun DI.Builder.implementation() {}
}
