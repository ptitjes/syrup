package io.github.ptitjes.syrup.specification

import io.github.ptitjes.syrup.specification.PluginSpecification.ExtensionContributionDeclaration
import org.kodein.di.bindings.NoArgBindingDI

/**
 * DSL builder for contributing to a [ExtensionPoint.Plural] extension point.
 */
@PluginSpecificationDsl
public class PluralContributionBuilder<T : Any> @PublishedApi internal constructor(
    /**
     * The extension point being contributed to.
     */
    @PublishedApi internal val point: ExtensionPoint.Plural<T>,
) {
    @PublishedApi
    internal val contributions: MutableList<ExtensionContributionDeclaration<T>> = mutableListOf()

    /**
     * Add a singleton of type [S] to the set of contributions for the extension point.
     *
     * The type [S] will be auto-constructed using Kodein.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmName("contributionGeneric")
    public inline fun <reified S : T> contribution() {
        contributions += ExtensionContributionDeclaration(
            point = point,
            type = point.type,
            creator = null
        )
    }

    /**
     * Add an instance of type [T] to the set of contributions for the extension point.
     *
     * The type [T] will be auto-constructed using Kodein.
     */
    @Suppress("UNCHECKED_CAST")
    public fun contribution() {
        contributions += ExtensionContributionDeclaration(
            point = point,
            type = point.type,
            creator = null
        )
    }

    /**
     * Add an instance to the set of contributions for the extension point using an explicit [creator].
     *
     * @param creator The function to create the instance of [T].
     */
    public fun contribution(
        creator: NoArgBindingDI<Any>.() -> T,
    ) {
        contributions += ExtensionContributionDeclaration(
            point = point,
            type = point.type,
            creator = creator
        )
    }
}
