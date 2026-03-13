package io.github.ptitjes.host.internal

import org.kodein.di.DI
import org.kodein.di.bindings.DIBinding
import org.kodein.type.TypeToken

internal fun collectBoundTypes(module: DI.Module): Map<TypeToken<*>, Boolean> {
    val boundTypeCollector = BoundTypeCollector()
    val wrappedModule = module.rewriteWithCallback(boundTypeCollector)
    DI.direct { import(wrappedModule) }
    return boundTypeCollector.boundSetTypes
}

internal fun DI.Module.rewriteWithCallback(
    boundTypeCollector: BoundTypeCollector? = null
): DI.Module = wrap { builder ->
    DiBuilderSetBindingRewriter(
        delegate = builder,
        onSetBinding = boundTypeCollector,
    )
}

internal interface OnSetBindingCallback {
    operator fun <A, T : Any> DI.Builder.invoke(
        tag: Any?,
        overrides: Boolean?,
        argType: TypeToken<in A>,
        type: TypeToken<out T>,
        isBinding: Boolean,
    )
}

internal class BoundTypeCollector : OnSetBindingCallback {
    private val _boundSetTypes = mutableMapOf<TypeToken<*>, Boolean>()
    val boundSetTypes: Map<TypeToken<*>, Boolean> get() = _boundSetTypes.toMap()

    override fun <A, T : Any> DI.Builder.invoke(
        tag: Any?,
        overrides: Boolean?,
        argType: TypeToken<in A>,
        type: TypeToken<out T>,
        isBinding: Boolean
    ) {
        _boundSetTypes[type] = isBinding
    }
}

/**
 * A [DI.Builder] wrapper that rewrites binding set operations and notifies a callback.
 * All the set bindings are force-created.
 */
private class DiBuilderSetBindingRewriter(
    val delegate: DI.Builder,
    val onSetBinding: OnSetBindingCallback? = null,
) : DI.Builder by delegate {
    private fun <A, T : Any> notifySetBinding(
        tag: Any?,
        overrides: Boolean?,
        argType: TypeToken<in A>,
        type: TypeToken<out T>,
        isBinding: Boolean,
    ) {
        if (onSetBinding != null) {
            with(onSetBinding) {
                invoke(tag, overrides, argType, type, isBinding)
            }
        }
    }

    override fun <T : Any> AddBindInSet(
        tag: Any?,
        overrides: Boolean?,
        binding: DIBinding<*, *, T>
    ) {
        notifySetBinding(tag, overrides, binding.argType, binding.createdType, isBinding = true)
        delegate.BindInSet(null, overrides, binding.createdType) { }
        delegate.AddBindInSet(tag, overrides, binding)
    }

    override fun <T : Any> BindInSet(
        tag: Any?,
        overrides: Boolean?,
        type: TypeToken<out T>,
        creator: DI.Builder.SetBinder<T>.() -> Unit
    ) {
        notifySetBinding(tag, overrides, TypeToken.Unit, type, isBinding = true)
        delegate.BindInSet(tag, overrides, type, creator)
    }

    override fun <T : Any> InBindSet(
        tag: Any?,
        overrides: Boolean?,
        type: TypeToken<out T>,
        creator: DI.Builder.SetBinder<T>.() -> Unit
    ) {
        notifySetBinding(tag, overrides, TypeToken.Unit, type, isBinding = false)
        delegate.BindInSet(tag, overrides, type, creator)
    }

    override fun <A : Any, T : Any> BindInArgSet(
        tag: Any?,
        overrides: Boolean?,
        argType: TypeToken<in A>,
        type: TypeToken<out T>,
        creator: DI.Builder.ArgSetBinder<A, T>.() -> Unit
    ) {
        notifySetBinding(tag, overrides, argType, type, isBinding = true)
        delegate.BindInArgSet(tag, overrides, argType, type, creator)
    }

    override fun <A : Any, T : Any> InBindArgSet(
        tag: Any?,
        overrides: Boolean?,
        argType: TypeToken<in A>,
        type: TypeToken<out T>,
        creator: DI.Builder.ArgSetBinder<A, T>.() -> Unit
    ) {
        notifySetBinding(tag, overrides, argType, type, isBinding = false)
        delegate.BindInArgSet(tag, overrides, argType, type, creator)
    }
}