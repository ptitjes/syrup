package io.github.ptitjes.host.internal

import org.kodein.di.DI

interface BindingSource {
    fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T>
}

fun BindingSource.onlyIf(predicate: (DI.Key<*, *, *>) -> Boolean): BindingSource = object : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> =
        debug("onlyIf", "") {
            val result = predicate(key)
            debug("[onlyIf] Predicate for $key returned $result")
            if (result) this@onlyIf.factories(key) else emptySequence()
        }
}

fun List<BindingSource>.flattenBindingSources(): BindingSource = object : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> =
        debug("flatten", "") {
            debug("[flatten] Flattening ${this@flattenBindingSources.size} sources")
            fold(emptySequence()) { acc, source -> acc + source.factories(key) }
        }
}

fun DI.asSource(debugName: String): BindingSource =
    DiBindingSource(this, debugName)

class DiBindingSource(
    private val di: DI,
    private val debugName: String,
) : BindingSource {
    override fun <C : Any, A, T : Any> factories(key: DI.Key<C, A, T>): Sequence<(A) -> T> =
        debug(debugName, "DiBindingSource") {
            debug("[$debugName] Collecting factories for $key")

            val contributions = di.allFactories(key)

            debug("[$debugName] Found ${contributions.size} factories")

            return@debug contributions.asSequence()
        }
}
