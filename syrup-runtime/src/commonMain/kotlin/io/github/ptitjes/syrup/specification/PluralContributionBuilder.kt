package io.github.ptitjes.syrup.specification

import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.type.TypeToken
import org.kodein.type.generic

/**
 * DSL builder for contributing to a [ExtensionPoint.Plural] extension point.
 */
class PluralContributionBuilder<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val point: ExtensionPoint.Plural<T>,
) {
    @PublishedApi
    internal val contributions = mutableListOf<PluginSpecification.ExtensionContributionDeclaration<T>>()

    /**
     * Add a singleton of type [S] to the set of contributions for the extension point.
     *
     * The type [S] will be auto-constructed using Kodein.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmName("contributionGeneric")
    inline fun <reified S : T> contribution() {
        contributions += PluginSpecification.ExtensionContributionDeclaration<T>(
            point = point,
            type = generic<S>() as TypeToken<out T>,
            creator = null
        )
    }

    /**
     * Add an instance of type [T] to the set of contributions for the extension point.
     *
     * The type [T] will be auto-constructed using Kodein.
     */
    @Suppress("UNCHECKED_CAST")
    fun contribution() {
        contributions += PluginSpecification.ExtensionContributionDeclaration<T>(
            point = point,
            type = point.type as TypeToken<out T>,
            creator = null
        )
    }

    /**
     * Add an instance to the set of contributions for the extension point using an explicit [creator].
     *
     * @param creator The function to create the instance of [T].
     */
    fun contribution(
        creator: NoArgBindingDI<Any>.() -> T,
    ) {
        contributions += PluginSpecification.ExtensionContributionDeclaration(
            point = point,
            type = null,
            creator = creator
        )
    }
}
