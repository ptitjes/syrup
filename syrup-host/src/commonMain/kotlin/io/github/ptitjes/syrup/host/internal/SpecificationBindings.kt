package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.specification.ExtensionPoint
import io.github.ptitjes.syrup.specification.PluginSpecification
import org.kodein.di.DI
import org.kodein.di.bindings.NoArgBindingDI
import org.kodein.di.bindings.Singleton
import org.kodein.type.TypeToken

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
        BindInSet(tag = point, overrides = null, type = point.type) { }
    }

    val contributedPluralPoints = specification.extensionContributions
        .map { it.point }
        .filterIsInstance<ExtensionPoint.Plural<*>>()
        .toSet()

    (contributedPluralPoints - ownedPluralPoints).forEach { point ->
        BindInSet(tag = point, overrides = null, type = point.type) { }
    }
}

internal fun DI.Builder.contributionBindingsFor(specification: PluginSpecification) {
    specification.extensionContributions.forEach { declaration ->
        val point = declaration.point
        val creator = declaration.creator

        if (point is ExtensionPoint.Singular<*>) {
            val createdType = point.type

            Bind(createdType, tag = point) with
                    Singleton(createdType, creator ?: { Instance(createdType, tag = point) })
        } else if (point is ExtensionPoint.Plural<*>) {
            val createdType = point.type

            InBindSet(tag = point, overrides = null, type = createdType) {
                add { Singleton(createdType, creator ?: { Instance(createdType, point) }) }
            }
        }
    }
}

private fun <C : Any, T : Any> DI.BindBuilder.WithScope<C>.Singleton(
    createdType: TypeToken<out T>,
    creator: NoArgBindingDI<C>.() -> T
) = Singleton(scope, contextType, explicitContext, createdType, creator = creator)
