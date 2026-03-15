package io.github.ptitjes.syrup.specification

import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.type.generic

/**
 * DSL builder for creating a [PluginSpecification].
 */
class PluginSpecificationBuilder {
    @PublishedApi
    internal val exposedTypes = mutableListOf<PluginSpecification.ExposedTypeDeclaration<*>>()

    @PublishedApi
    internal val extensionPoints = mutableListOf<PluginSpecification.ExtensionPointDeclaration<*>>()

    @PublishedApi
    internal val extensionContributions = mutableListOf<PluginSpecification.ExtensionContributionDeclaration<*>>()

    /**
     * Exposes a type [T] from this plugin.
     *
     * The type must be bound elsewhere in the plugin's implementation.
     */
    inline fun <reified T : Any> exposedType() {
        exposedTypes += PluginSpecification.ExposedTypeDeclaration(type = generic<T>(), creator = null)
    }

    /**
     * Exposes a type [T] from this plugin with an explicit [creator].
     *
     * @param creator The function to create the instance of [T].
     */
    inline fun <reified T : Any> exposedType(noinline creator: NoArgBindingDI<Any>.() -> T) {
        exposedTypes += PluginSpecification.ExposedTypeDeclaration(type = generic<T>(), creator = creator)
    }

    /**
     * Declares that this plugin owns a singular extension point.
     *
     * @param point The [ExtensionPoint.Singular] instance.
     * @param optional Whether the contribution is optional.
     */
    fun <T : Any> extensionPoint(point: ExtensionPoint.Singular<T>, optional: Boolean = false) {
        extensionPoints += PluginSpecification.ExtensionPointDeclaration<T>(point = point, optional = optional)
    }

    /**
     * Declares that this plugin owns a plural extension point.
     *
     * @param point The [ExtensionPoint.Plural] instance.
     * @param optional Whether the contributions are optional (resulting in an empty set).
     */
    fun <T : Any> extensionPoint(point: ExtensionPoint.Plural<T>, optional: Boolean = false) {
        extensionPoints += PluginSpecification.ExtensionPointDeclaration<T>(point = point, optional = optional)
    }

    /**
     * Contributes to a [ExtensionPoint.Singular] extension point.
     *
     * @param block The builder block for the contribution.
     */
    operator fun <T : Any> ExtensionPoint.Singular<T>.invoke(block: SingularContributionBuilder<T>.() -> Unit) {
        val builder = SingularContributionBuilder<T>(this)
        builder.block()
        extensionContributions += builder.contributions
    }

    /**
     * Contributes to a [ExtensionPoint.Plural] extension point.
     *
     * @param block The builder block for the contributions.
     */
    operator fun <T : Any> ExtensionPoint.Plural<T>.invoke(block: PluralContributionBuilder<T>.() -> Unit) {
        val builder = PluralContributionBuilder<T>(this)
        builder.block()
        extensionContributions += builder.contributions
    }
}
