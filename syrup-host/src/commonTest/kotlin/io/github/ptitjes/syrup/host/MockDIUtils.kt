package io.github.ptitjes.syrup.host

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches
import org.kodein.di.DI
import org.kodein.type.TypeToken
import org.kodein.type.generic

fun <A, T : Any> bindingFactory(f: (A) -> T): (A) -> T = f

inline fun <reified T : Any> MokkeryMatcherScope.anyKeyWithType(): DI.Key<*, *, *> =
    anyKeyWithType(generic<T>())

fun MokkeryMatcherScope.anyKeyWithType(
    type: TypeToken<*>,
): DI.Key<*, *, *> = matches(
    toString = { "anyKeyWithType(${type.simpleDispString()})" },
    predicate = { it.type == type }
)

fun MokkeryMatcherScope.anyKey(): DI.Key<*, *, *> = matches(
    toString = { "anyKey()" },
    predicate = { true }
)
