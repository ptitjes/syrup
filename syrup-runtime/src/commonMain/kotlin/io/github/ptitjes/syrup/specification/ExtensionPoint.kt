package io.github.ptitjes.syrup.specification

import org.kodein.type.TypeToken

/**
 * Represents an extension point that can be contributed to by other plugins.
 *
 * A plugin defines extension points to allow other plugins to provide additional functionality.
 * There are two types of extension points:
 * - [Singular]: Exactly one contribution is expected (or optional).
 * - [Plural]: A set of contributions is expected (or optional).
 */
public abstract class ExtensionPoint<T : Any> private constructor() {
    public abstract val type: TypeToken<T>

    /**
     * An extension point that expects exactly one contribution.
     *
     * @param T The type of the contribution.
     */
    public open class Singular<T : Any>(override val type: TypeToken<T>) : ExtensionPoint<T>() {
        override fun toString(): String = "ExtensionPoint.Singular(type=$type)"
    }

    /**
     * An extension point that expects multiple contributions.
     *
     * @param T The type of the contributions.
     */
    public open class Plural<T : Any>(override val type: TypeToken<T>) : ExtensionPoint<T>() {
        override fun toString(): String = "ExtensionPoint.Plural(type=$type)"
    }
}
