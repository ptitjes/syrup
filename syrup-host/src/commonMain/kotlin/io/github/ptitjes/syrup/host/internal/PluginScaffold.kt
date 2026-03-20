package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.PluginContext
import io.github.ptitjes.syrup.Sourced
import io.github.ptitjes.syrup.host.Logger
import io.github.ptitjes.syrup.specification.ExtensionPoint
import io.github.ptitjes.syrup.specification.PluginSpecification
import org.kodein.di.*
import org.kodein.type.TypeToken
import org.kodein.type.erasedComp

/**
 * A [PluginScaffold] exposes the internal DI of a plugin and the two binding sources for the plugin's
 * contributions and exposed types.
 *
 * @param plugin The plugin.
 * @param specification The plugin specification.
 * @param internalBindings The bindings for the plugin's internal implementation.
 */
internal class PluginScaffold(
    private val plugin: Plugin,
    private val specification: PluginSpecification,
    internalBindings: DI.Builder.() -> Unit,
    private val logger: Logger = NoopLogger,
) {
    private val internalModule = DI.Module(name = "${plugin.id}#internal") { internalBindings() }

    private val context: PluginContext = object : PluginContext {
        override fun <T : Any> contribution(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T> =
            sourcedContribution(extensionPoint).map { sourced -> sourced.contribution }

        override fun <T : Any> contributionOrNull(extensionPoint: ExtensionPoint.Singular<T>): LazyDelegate<T?> =
            sourcedContributionOrNull(extensionPoint).map { sourced -> sourced?.contribution }

        override fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>> =
            sourcedContributions(extensionPoint).map { sourced -> sourced.map { it.contribution }.toSet() }

        override fun <T : Any> sourcedContribution(
            extensionPoint: ExtensionPoint.Singular<T>,
        ): LazyDelegate<Sourced<T>> {
            val contributionType = extensionPoint.type
            val sourcedType = sourcedContributionTypeOf(contributionType)
            return contributionsDI.Instance(tag = extensionPoint, type = sourcedType)
        }

        override fun <T : Any> sourcedContributionOrNull(
            extensionPoint: ExtensionPoint.Singular<T>,
        ): LazyDelegate<Sourced<T>?> {
            val contributionType = extensionPoint.type
            val sourcedType = sourcedContributionTypeOf(contributionType)
            return contributionsDI.InstanceOrNull(tag = extensionPoint, type = sourcedType)
        }

        override fun <T : Any> sourcedContributions(
            extensionPoint: ExtensionPoint.Plural<T>,
        ): LazyDelegate<Set<Sourced<T>>> {
            val contributionType = extensionPoint.type
            val sourcedType = sourcedContributionTypeOf(contributionType)
            @Suppress("UNCHECKED_CAST")
            val type = erasedComp(Set::class, sourcedType) as TypeToken<Set<Sourced<T>>>
            return contributionsDI.Instance(tag = extensionPoint, type = type)
        }
    }

    private var bindingSource: BindingSource? = null

    val internalDi = ModularDI(
        debugName = "${plugin.id}#internal",
        sourceDi = null,
        callbackDi = null,
        additionalBindings = {
            bind<PluginContext> { instance(context) }
            import(internalModule)
        },
        bindingSourceProvider = { bindingSource ?: NoopBindingSource },
        logger = logger,
    )

    private val exposedTypesDI = ModularDI(
        debugName = "${plugin.id}#exposed-types",
        sourceDi = DI {
            exposedTypeBindingsFor(specification)
        },
        callbackDi = internalDi,
        bindingSourceProvider = { bindingSource ?: NoopBindingSource },
        logger = logger,
    )

    private val exposedTypeTokens = specification.exposedTypeTokens
    val exposedTypes = exposedTypesDI.asSource()
        .filter { key -> key.type in exposedTypeTokens }

    private val contributionsDI = ModularDI(
        debugName = "${plugin.id}#contributions",
        sourceDi = DI {
            extensionPointBindingsFor(specification)
            contributionBindingsFor(specification, source = plugin)
        },
        callbackDi = internalDi,
        bindingSourceProvider = { bindingSource ?: NoopBindingSource },
        logger = logger,
    )

    private val contributedExtensionPoints = specification.contributedExtensionPoints
    val contributions = contributionsDI.asSource()
        .filter { key -> key.tag is ExtensionPoint<*> && key.tag in contributedExtensionPoints }

    /**
     * Wire the scaffold with the incoming exposed types and contributions.
     *
     * @param dependencies The scaffolds of the plugins that this plugin depends on.
     * @param dependents The scaffolds of the plugins that depend on this plugin.
     */
    internal fun wire(dependencies: Iterable<PluginScaffold>, dependents: Iterable<PluginScaffold>) {
        bindingSource = buildList {
            dependencies.forEach { dependency ->
                add(dependency.exposedTypes)
            }

            val ownedExtensionPoints = specification.ownedExtensionPoints
            dependents.forEach { dependent ->
                add(
                    dependent.contributions
                        .filter { key -> key.tag is ExtensionPoint<*> && key.tag in ownedExtensionPoints }
                )
            }
        }.flattenBindingSources()
    }
}

private val PluginSpecification.ownedExtensionPoints: List<ExtensionPoint<*>>
    get() = extensionPoints.map { it.point }

private val PluginSpecification.contributedExtensionPoints: List<ExtensionPoint<*>>
    get() = extensionContributions.map { it.point }

private val PluginSpecification.exposedTypeTokens: List<TypeToken<out Any>>
    get() = exposedTypes.map { it.type }
