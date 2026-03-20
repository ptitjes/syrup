package io.github.ptitjes.syrup.host

/**
 * Exception thrown by the Syrup host when a plugin error occurs.
 */
public class PluginException(message: String, cause: Throwable? = null) : Exception(message, cause)
