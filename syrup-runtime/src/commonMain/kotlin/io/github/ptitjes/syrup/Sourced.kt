package io.github.ptitjes.syrup

data class Sourced<T : Any>(
    val source: Plugin,
    val contribution: T,
)
