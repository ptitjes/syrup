package io.github.ptitjes.syrup.host.internal

import org.kodein.di.*
import org.kodein.di.bindings.BindingDI
import org.kodein.di.bindings.ErasedContext
import org.kodein.di.bindings.toKContext
import org.kodein.type.TypeToken
import org.kodein.type.generic

internal fun DI.Key<*, *, *>.isSetKey(): Boolean = type.isSetType()

private val SET_STAR = generic<Set<*>>()
private fun TypeToken<out Any>.isSetType(): Boolean = SET_STAR.isAssignableFrom(this)

@Suppress("UNCHECKED_CAST")
internal fun <C : Any, A, T : Any> DI.retrieveFactories(
    key: DI.Key<C, A, T>,
    callbackDi: DI,
    overrideLevel: Int = 0,
): List<(A) -> T> {
    val result = container.tree.find(key, overrideLevel, all = true)
    val allFactories = result.map { (key, definition, translator) ->
        val currentContext = diContext as C
        val originalContext = DIContext.Companion(key.contextType, currentContext) as DIContext<Any>
        val kContext = translator?.toKContext(callbackDi.direct, currentContext) ?: originalContext
        val bindingDI = BindingDIImpl(callbackDi.direct.on(kContext.value), key, overrideLevel)
        definition.binding.getFactory(key, bindingDI)
    }
    return allFactories
}

internal fun <A, C : Any, T : Any> DI.allFactories(key: DI.Key<C, A, T>): List<(A) -> T> =
    direct.AllFactories(key.argType, key.type, key.tag) + allExternalFactories(key)

@Suppress("UNCHECKED_CAST")
private fun <A, C : Any, T : Any> DI.allExternalFactories(key: DI.Key<C, A, T>): List<(A) -> T> =
    container.tree.externalSources.flatMap { source ->
        val bindingDi = BindingDIImpl(this.direct, key, 0)
        source.getFactory(bindingDi, key)?.let { factory -> listOf(factory as ((A) -> T)) }
            ?: emptyList<(A) -> T>()
    }

@Suppress("UNCHECKED_CAST")
private class BindingDIImpl<out C : Any, out A, out T : Any>(
    override val directDI: DirectDI,
    private val key: DI.Key<C, A, T>,
    private val overrideLevel: Int
) : DirectDI by directDI, BindingDI<C> {
    override fun overriddenFactory(): (Any?) -> Any =
        container.factory(key, context, overrideLevel + 1) as (Any?) -> Any

    override fun overriddenFactoryOrNull(): ((Any?) -> Any)? =
        container.factoryOrNull(key, context, overrideLevel + 1) as ((Any?) -> Any)?

    override val context: C get() = directDI.di.diContext.value as C
    override fun onErasedContext(): BindingDI<C> = BindingDIImpl(
        directDI.On(ErasedContext),
        key,
        overrideLevel
    )
}
