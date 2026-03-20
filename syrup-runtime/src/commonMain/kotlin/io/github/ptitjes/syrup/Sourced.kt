package io.github.ptitjes.syrup

/**
 * Represents a [contribution] made to an extension point, along with the [source] plugin that made it.
 */
public data class Sourced<T : Any>(
    val source: Plugin,
    val contribution: T,
)
