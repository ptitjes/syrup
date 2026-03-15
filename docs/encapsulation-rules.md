# Encapsulation Rules for Syrup

Syrup organizes your application as a set of plugins. Each plugin can define its own API, internal implementations, and extension points.

## Overview

Syrup creates an `internalDi` for each plugin that will be used for any injection inside the plugin.

Additionally, Syrup will create two binding sources for the plugin:
- one called `exposed-types` to transmit its exposed types to the plugin's dependencies, and
- another called `contributions` to transmit its extension point contributions to the plugins defining these extension points.

Syrup connects everything by:
- wiring a plugin's `exposed-types` binding source to the plugin's dependencies
- wiring a plugin's `contributions` binding source to the plugin's dependents
- aggregating the `exposed-types` from a plugin itself and its dependencies and making them available to the plugin's
  `internalDi`
- aggregating the `contributions` from a plugin and its dependents and making them available to the plugin's `internalDi`
  when that plugin owns the extension point

Finally, Syrup creates a global `mainDi` that will expose the `exposed-types` of all the plugins.

## Encapsulation Rules

To ensure modularity and predictable behavior, the following encapsulation rules are formalized:

| Definitions                   | Visible By                | Visible From                                 |
|-------------------------------|---------------------------|----------------------------------------------|
| Internal bindings             | itself                    | `internalDi`                                 |
| Exposed types                 | depenpencies              | `internalDi`                                 |

Contributions are visible for the plugin that defines the extension point through their `PluginContext`,
which is available for injection through the `internalDi`.
