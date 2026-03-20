package io.github.ptitjes.syrup

public data class Sourced<T : Any>(
    val source: Plugin,
    val contribution: T,
)
