package io.github.ptitjes.syrup

import io.github.ptitjes.syrup.specification.ExtensionPoint
import org.kodein.di.LazyDelegate

interface PluginContext {
    fun <T : Any> contribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T>
    fun <T : Any> contributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T?>
    fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>>
}
