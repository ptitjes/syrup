package io.github.ptitjes.syrup

import io.github.ptitjes.syrup.specification.ExtensionPoint
import org.kodein.di.LazyDelegate

/**
 * Provides access to the contributions of the plugin's extension points.
 *
 * This context is available to the bindings defined in the [Plugin.implementation] block
 * and can thus be injected into internal services.
 */
public interface PluginContext {
    /**
     * Returns the contribution of the given singular [extensionPoint].
     *
     * @throws org.kodein.di.DI.NotFoundException if the extension point is not optional and has no contribution.
     */
    public fun <T : Any> contribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T>

    /**
     * Returns the optional contribution of the given singular [extensionPoint].
     */
    public fun <T : Any> contributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T?>

    /**
     * Returns the set of contributions of the given plural [extensionPoint].
     *
     * @throws org.kodein.di.DI.NotFoundException if the extension point is not optional and has no contributions.
     */
    public fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>>

    /**
     * Returns the sourced contribution of the given singular [extensionPoint].
     *
     * The [Sourced] wrapper provides access to the plugin that made the contribution.
     */
    public fun <T : Any> sourcedContribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<Sourced<T>>

    /**
     * Returns the optional sourced contribution of the given singular [extensionPoint].
     */
    public fun <T : Any> sourcedContributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<Sourced<T>?>

    /**
     * Returns the set of sourced contributions of the given plural [extensionPoint].
     */
    public fun <T : Any> sourcedContributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<Sourced<T>>>
}
