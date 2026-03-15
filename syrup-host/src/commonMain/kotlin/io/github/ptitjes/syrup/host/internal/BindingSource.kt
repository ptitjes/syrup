package io.github.ptitjes.syrup.host.internal

import org.kodein.di.DI

internal interface BindingSource {
    fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T>
}

internal object NoopBindingSource : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> = emptySequence()
}

internal fun BindingSource.filter(predicate: (DI.Key<*, *, *>) -> Boolean): BindingSource = object : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> =
        if (predicate(key)) this@filter.factories(key) else emptySequence()
}

internal fun List<BindingSource>.flattenBindingSources(): BindingSource = object : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> =
        fold(emptySequence()) { acc, source -> acc + source.factories(key) }
}

internal fun DI.asSource(): BindingSource = DiBindingSource(this)

private class DiBindingSource(
    private val di: DI,
) : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> =
        di.allFactories(key).asSequence()
}
