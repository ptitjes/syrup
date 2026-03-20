package io.github.ptitjes.syrup.specification

import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.type.TypeToken

/**
 * A specification of a plugin's API, extension points, and contributions.
 *
 * Use the [companion object's invoke][PluginSpecification.Companion.invoke] to create a new specification
 * using the DSL.
 */
public class PluginSpecification private constructor(
    public val exposedTypes: List<ExposedTypeDeclaration<*>>,
    public val extensionPoints: List<ExtensionPointDeclaration<*>>,
    public val extensionContributions: List<ExtensionContributionDeclaration<*>>,
) {
    public companion object {
        /**
         * Create a new [PluginSpecification] using a DSL.
         *
         * @param builderAction The builder DSL.
         * @return The created [PluginSpecification].
         */
        public operator fun invoke(
            builderAction: PluginSpecificationBuilder.() -> Unit,
        ): PluginSpecification {
            val builder = PluginSpecificationBuilder()
            builder.builderAction()
            return PluginSpecification(
                exposedTypes = builder.exposedTypes.toList(),
                extensionPoints = builder.extensionPoints.toList(),
                extensionContributions = builder.extensionContributions.toList(),
            )
        }
    }

    override fun toString(): String {
        return "PluginSpecification(exposedTypes=$exposedTypes, extensionPoints=$extensionPoints, extensionContributions=$extensionContributions)"
    }

    /**
     * A declaration of a type exposed by the plugin.
     *
     * @param T The type of the exposed object.
     * @property type The Kodein [TypeToken] for the type.
     * @property creator An optional creator function to bind the type in the DI container.
     */
    public class ExposedTypeDeclaration<T : Any> @PublishedApi internal constructor(
        public val type: TypeToken<T>,
        public val creator: (NoArgBindingDI<Any>.() -> T)?,
    ) {
        override fun toString(): String = "ExposedType(type=$type)"
    }

    /**
     * A declaration of an extension point owned by the plugin.
     *
     * @param T The type of the extension point's contributions.
     * @property point The [ExtensionPoint] instance.
     * @property optional Whether the extension point is optional.
     */
    public class ExtensionPointDeclaration<T : Any> @PublishedApi internal constructor(
        public val point: ExtensionPoint<T>,
        public val optional: Boolean,
    ) {
        override fun toString(): String = "ExtensionPoint(point=$point, optional=$optional)"
    }

    /**
     * A declaration of a contribution to an extension point.
     *
     * @param T The type of the contribution.
     * @property point The [ExtensionPoint] being contributed to.
     * @property type The Kodein [TypeToken] for the contribution type (if auto-constructed).
     * @property creator An optional creator function for the contribution.
     */
    public class ExtensionContributionDeclaration<T : Any> @PublishedApi internal constructor(
        public val point: ExtensionPoint<T>,
        public val type: TypeToken<T>,
        public val creator: (NoArgBindingDI<Any>.() -> T)?,
    ) {
        override fun toString(): String = "Contribution(point=$point)"
    }
}
