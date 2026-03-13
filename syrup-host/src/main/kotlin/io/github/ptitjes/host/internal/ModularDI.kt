package io.github.ptitjes.host.internal

import org.kodein.di.DI
import org.kodein.di.bindings.BindingDI
import org.kodein.di.bindings.ExternalSource
import org.kodein.di.bindings.externalFactory

@Suppress("FunctionName")
internal fun ModularDI(
    debugName: String,
    sourceDi: DI,
    callbackDi: DI?,
    bindingSourceProvider: (() -> BindingSource)? = null,
) = DI {
    externalSources += ModularSource(
        debugName = debugName,
        sourceDi = sourceDi,
        callbackDi = callbackDi,
        bindingSourceProvider = bindingSourceProvider,
    )
}

private class ModularSource(
    val debugName: String,
    private val sourceDi: DI,
    private val callbackDi: DI?,
    private val bindingSourceProvider: (() -> BindingSource)? = null,
) : ExternalSource {
    override fun getFactory(di: BindingDI<*>, key: DI.Key<*, *, *>): ((Any?) -> Any)? =
        debug(debugName, "getFactory") {
            debug("[$debugName] Collecting factories for $key")

            @Suppress("UNCHECKED_CAST")
            val factoriesFromSource = sourceDi.retrieveFactories(
                debugName = debugName,
                key = key,
                callbackDi = callbackDi ?: di.di,
            ) as List<(Any?) -> Any>

            debug("[$debugName] Found ${factoriesFromSource.size} factories from source")

            @Suppress("UNCHECKED_CAST")
            val factoriesFromProvider = bindingSourceProvider?.let { provider ->
                debug("[$debugName] Collecting factories from provider")
                provider.invoke().factories(key) as Sequence<(Any?) -> Any>
            } ?: emptySequence()

            val factories = factoriesFromSource.asSequence() + factoriesFromProvider

            return@debug factories.aggregateFor(key)
        }
}

private fun Sequence<(Any?) -> Any>.aggregateFor(key: DI.Key<*, *, *>): ((Any?) -> Any)? =
    if (key.isSetKey()) {
        externalFactory { arg ->
            fold(emptySet<Any>()) { set, factory ->
                @Suppress("UNCHECKED_CAST")
                val elements = factory(arg) as Set<Any>
                set + elements
            }
        }
    } else {
        firstOrNull()
    }
