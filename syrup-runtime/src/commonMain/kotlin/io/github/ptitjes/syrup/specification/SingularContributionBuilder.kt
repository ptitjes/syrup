package io.github.ptitjes.syrup.specification

import io.github.ptitjes.syrup.specification.PluginSpecification.ExtensionContributionDeclaration
import org.kodein.di.bindings.NoArgBindingDI

/**
 * DSL builder for contributing to a [ExtensionPoint.Singular] extension point.
 */
@PluginSpecificationDsl
public class SingularContributionBuilder<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val point: ExtensionPoint.Singular<T>,
) {
    @PublishedApi
    internal val contributions: MutableList<ExtensionContributionDeclaration<T>> = mutableListOf()

    /**
     * Contribute a singleton of type [S] to the extension point.
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
     * Contribute an instance of type [T] to the extension point.
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
     * Contribute an instance to the extension point using an explicit [creator].
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
