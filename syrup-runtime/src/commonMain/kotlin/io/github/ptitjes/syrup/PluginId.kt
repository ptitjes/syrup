package io.github.ptitjes.syrup

/**
 * A unique identifier for a [Plugin].
 */
@JvmInline
public value class PluginId(public val value: String) {
    override fun toString(): String = value
}
