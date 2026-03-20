package io.github.ptitjes.syrup.host

import org.kodein.di.DI

/**
 * Manages the plugin lifecycle and provides access to the combined DI container.
 */
public interface PluginManager {
    /**
     * The main DI container that includes all plugin bindings.
     */
    public val mainDi: DI
}
