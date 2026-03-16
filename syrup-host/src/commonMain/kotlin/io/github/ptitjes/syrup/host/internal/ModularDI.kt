package io.github.ptitjes.syrup.host.internal

import io.github.ptitjes.syrup.host.Logger
import org.kodein.di.DI
import org.kodein.di.bindings.BindingDI
import org.kodein.di.bindings.ExternalSource
import org.kodein.di.bindings.externalFactory

@Suppress("FunctionName")
internal fun ModularDI(
    debugName: String,
    sourceDi: DI?,
    callbackDi: DI?,
    additionalBindings: DI.Builder.() -> Unit = {},
    bindingSourceProvider: (() -> BindingSource)? = null,
    logger: Logger,
) = DI {
    additionalBindings()

    externalSources += ModularSource(
        debugName = debugName,
        sourceDi = sourceDi,
        callbackDi = callbackDi,
        bindingSourceProvider = bindingSourceProvider,
        logger = logger,
    )
}

private class ModularSource(
    val debugName: String,
    private val sourceDi: DI?,
    private val callbackDi: DI?,
    private val bindingSourceProvider: (() -> BindingSource)? = null,
    private val logger: Logger,
) : ExternalSource {

    private val bindingSource by lazy { bindingSourceProvider?.invoke() }

    override fun getFactory(di: BindingDI<*>, key: DI.Key<*, *, *>): ((Any?) -> Any)? {
        logger.trace { "$debugName > Looking for key: $key" }

        @Suppress("UNCHECKED_CAST")
        val factoriesFromDI = (sourceDi ?: di.di).retrieveFactories(
            key = key,
            callbackDi = callbackDi ?: di.di,
        ) as List<(Any?) -> Any>

        logger.trace { "$debugName > Found ${factoriesFromDI.size} factories from DI" }

        @Suppress("UNCHECKED_CAST")
        val factoriesFromProvider =
            bindingSource?.let { it.factories(key) as Sequence<(Any?) -> Any> } ?: emptySequence()

        val factories = factoriesFromDI.asSequence() + factoriesFromProvider

        return factories.aggregate(key)
    }
}

private fun Sequence<(Any?) -> Any>.aggregate(key: DI.Key<*, *, *>): ((Any?) -> Any)? =
    if (key.isSetKey()) {
        val allFactories = toList()
        if (allFactories.isNotEmpty()) {
            externalFactory { arg ->
                val result = mutableSetOf<Any>()
                allFactories.forEach { factory ->
                    @Suppress("UNCHECKED_CAST")
                    val castFactory = factory as (Any?) -> Set<Any>
                    val elements = castFactory(arg)
                    result.addAll(elements)
                }
                result
            }
        } else {
            null
        }
    } else {
        firstOrNull()
    }
