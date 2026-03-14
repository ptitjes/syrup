package io.github.ptitjes.syrup.specification

import org.kodein.type.TypeToken

/**
 * Represents an extension point that can be contributed to by other plugins.
 *
 * Extension points are defined by a plugin to allow other plugins to provide
 * additional functionality. There are two types of extension points:
 * - [Singular]: Exactly one contribution is expected (or optional).
 * - [Plural]: A set of contributions is expected (or optional).
 */
abstract class ExtensionPoint private constructor() {
    /**
     * An extension point that expects exactly one contribution.
     *
     * @param T The type of the contribution.
     */
    open class Singular<T : Any>(val type: TypeToken<T>) : ExtensionPoint()

    /**
     * An extension point that expects multiple contributions.
     *
     * @param T The type of the contributions.
     */
    open class Plural<T : Any>(val type: TypeToken<T>) : ExtensionPoint()
}
