package io.github.ptitjes.host.internal

private var indent = 0

internal fun debug(message: String) {
    println("   ".repeat(indent) + message)
}

internal inline fun <T> debug(debugName: String, blocName: String, block: () -> T): T {
    try {
        debug("[$debugName] >> $blocName")
        return indented {
            block().also { result ->
                debug("[$debugName] Result: $result")
            }
        }
    } finally {
        debug("[$debugName] << $blocName")
    }
}

private inline fun <T> indented(block: () -> T): T {
    indent++
    try {
        return block()
    } finally {
        indent--
    }
}
