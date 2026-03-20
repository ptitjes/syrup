package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.Plugin
import io.github.ptitjes.syrup.Sourced
import io.github.ptitjes.syrup.specification.ExtensionPoint
import io.github.ptitjes.syrup.specification.PluginSpecification
import org.kodein.di.DI
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.bindings.Singleton
import org.kodein.type.TypeToken
import org.kodein.type.erasedComp

internal fun DI.Builder.exposedTypeBindingsFor(specification: PluginSpecification) {
    specification.exposedTypes.forEach { declaration ->
        val createdType = declaration.type
        val creator = declaration.creator

        Bind(type = createdType) with Singleton(createdType, creator ?: { Instance(createdType) })
    }
}

internal fun DI.Builder.extensionPointBindingsFor(
    specification: PluginSpecification
) {
    val ownedPluralPoints = specification.extensionPoints
        .map { it.point }
        .filterIsInstance<ExtensionPoint.Plural<*>>()
        .toSet()

    ownedPluralPoints.forEach { point ->
        BindInSet(tag = point, type = sourcedContributionTypeOf(point.type)) { }
    }

    val contributedPluralPoints = specification.extensionContributions
        .map { it.point }
        .filterIsInstance<ExtensionPoint.Plural<*>>()
        .toSet()

    (contributedPluralPoints - ownedPluralPoints).forEach { point ->
        // We bind a set-binding even if we don't own the extension point
        // because we want to allow contributions to be added to the set later.
        BindInSet(tag = point, type = sourcedContributionTypeOf(point.type)) { }
    }
}

internal fun DI.Builder.contributionBindingsFor(specification: PluginSpecification, source: Plugin) {
    specification.extensionContributions.forEach { declaration ->
        contributionBindingsFor(declaration, source)
    }
}

private fun <T : Any> DI.Builder.contributionBindingsFor(
    declaration: PluginSpecification.ExtensionContributionDeclaration<T>,
    source: Plugin,
) {
    val point = declaration.point
    val creator = declaration.creator
    val contributionType = declaration.type
    val createdType = sourcedContributionTypeOf(contributionType)

    if (point is ExtensionPoint.Singular<*>) {
        Bind(tag = point, type = createdType) with Singleton(createdType) {
            Sourced(
                source = source,
                contribution = creator?.invoke(this) ?: Instance(contributionType, tag = point),
            )
        }
    } else if (point is ExtensionPoint.Plural<*>) {
        InBindSet(tag = point, type = createdType) {
            add {
                Singleton(createdType) {
                    Sourced(
                        source = source,
                        contribution = creator?.invoke(this) ?: Instance(contributionType, tag = point),
                    )
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> sourcedContributionTypeOf(type: TypeToken<out T>): TypeToken<Sourced<T>> =
    erasedComp(Sourced::class, type) as TypeToken<Sourced<T>>

private fun <C : Any, T : Any> DI.BindBuilder.WithScope<C>.Singleton(
    createdType: TypeToken<out T>,
    creator: NoArgBindingDI<C>.() -> T
) = Singleton(scope, contextType, explicitContext, createdType, creator = creator)
