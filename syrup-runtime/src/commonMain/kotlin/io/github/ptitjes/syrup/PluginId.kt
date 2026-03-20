package io.github.ptitjes.syrup

@JvmInline
public value class PluginId(public val value: String) {
    override fun toString(): String = value
}
