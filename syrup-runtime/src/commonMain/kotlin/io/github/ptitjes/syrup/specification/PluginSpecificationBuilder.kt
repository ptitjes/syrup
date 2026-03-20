package io.github.ptitjes.syrup.specification

import io.github.ptitjes.syrup.specification.PluginSpecification.*
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.type.generic

/**
 * DSL builder for creating a [PluginSpecification].
 */
@PluginSpecificationDsl
public class PluginSpecificationBuilder {
    @PublishedApi
    internal val exposedTypes: MutableList<ExposedTypeDeclaration<*>> = mutableListOf()

    @PublishedApi
    internal val extensionPoints: MutableList<ExtensionPointDeclaration<*>> = mutableListOf()

    @PublishedApi
    internal val extensionContributions: MutableList<ExtensionContributionDeclaration<*>> = mutableListOf()

    /**
     * Exposes a type [T] from this plugin.
     *
     * The type must be bound elsewhere in the plugin's implementation.
     */
    public inline fun <reified T : Any> exposedType() {
        exposedTypes += ExposedTypeDeclaration(type = generic<T>(), creator = null)
    }

    /**
     * Exposes a type [T] from this plugin with an explicit [creator].
     *
     * @param creator The function to create the instance of [T].
     */
    public inline fun <reified T : Any> exposedType(noinline creator: NoArgBindingDI<Any>.() -> T) {
        exposedTypes += ExposedTypeDeclaration(type = generic<T>(), creator = creator)
    }

    /**
     * Declares that this plugin owns a singular extension point.
     *
     * @param point The [ExtensionPoint.Singular] instance.
     * @param optional Whether the contribution is optional.
     */
    public fun <T : Any> extensionPoint(point: ExtensionPoint.Singular<T>, optional: Boolean = false) {
        extensionPoints += ExtensionPointDeclaration(point = point, optional = optional)
    }

    /**
     * Declares that this plugin owns a plural extension point.
     *
     * @param point The [ExtensionPoint.Plural] instance.
     * @param optional Whether the contributions are optional (resulting in an empty set).
     */
    public fun <T : Any> extensionPoint(point: ExtensionPoint.Plural<T>, optional: Boolean = false) {
        extensionPoints += ExtensionPointDeclaration(point = point, optional = optional)
    }

    /**
     * Contributes to a [ExtensionPoint.Singular] extension point.
     *
     * @param block The builder block for the contribution.
     */
    public operator fun <T : Any> ExtensionPoint.Singular<T>.invoke(block: SingularContributionBuilder<T>.() -> Unit) {
        val builder = SingularContributionBuilder<T>(this)
        builder.block()
        extensionContributions += builder.contributions
    }

    /**
     * Contributes to a [ExtensionPoint.Plural] extension point.
     *
     * @param block The builder block for the contributions.
     */
    public operator fun <T : Any> ExtensionPoint.Plural<T>.invoke(block: PluralContributionBuilder<T>.() -> Unit) {
        val builder = PluralContributionBuilder<T>(this)
        builder.block()
        extensionContributions += builder.contributions
    }
}
