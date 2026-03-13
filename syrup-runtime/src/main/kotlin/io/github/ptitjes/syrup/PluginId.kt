package io.github.ptitjes.syrup

@JvmInline
value class PluginId(val value: String) {
    override fun toString(): String = value
}
