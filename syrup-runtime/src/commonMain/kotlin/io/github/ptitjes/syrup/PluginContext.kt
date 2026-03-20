package io.github.ptitjes.syrup

import io.github.ptitjes.syrup.specification.ExtensionPoint
import org.kodein.di.LazyDelegate

public interface PluginContext {
    public fun <T : Any> contribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T>
    public fun <T : Any> contributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T?>
    public fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>>

    public fun <T : Any> sourcedContribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<Sourced<T>>
    public fun <T : Any> sourcedContributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<Sourced<T>?>
    public fun <T : Any> sourcedContributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<Sourced<T>>>
}
