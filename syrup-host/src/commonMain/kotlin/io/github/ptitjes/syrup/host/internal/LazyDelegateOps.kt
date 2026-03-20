package io.github.ptitjes.syrup.host.internal

import org.kodein.di.LazyDelegate
import kotlin.reflect.KProperty

internal fun <T, R> LazyDelegate<T>.map(mapAction: (T) -> R): LazyDelegate<R> = MappedLazyDelegate(this, mapAction)

private class MappedLazyDelegate<T, R>(
    private val delegate: LazyDelegate<T>,
    private val mapAction: (T) -> R,
) : LazyDelegate<R> {
    override fun provideDelegate(receiver: Any?, prop: KProperty<Any?>): Lazy<R> = lazy {
        val value by delegate
        mapAction(value)
    }
}
