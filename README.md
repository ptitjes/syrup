# Syrup

Syrup is a lightweight plugin system for Kotlin Multiplatform.

Plugin objects are discovered automatically at runtime via a ServiceLoader mechanism
(powered by [sweet-spi](https://github.com/whyoleg/sweet-spi)).
They then participate in building a graph of dependency injection (DI) containers
(powered by [Kodein](https://github.com/kosi-libs/Kodein)).

## How it works

Syrup organizes your application as a set of plugins. Each plugin:

- declares its dependencies on other plugins,
- exposes bindings (via `api()`) that are visible to plugins that depend on it,
- declares internal bindings (via `implementation()`) that are private to the plugin,
- can expose set-bindings to collect contributions from its dependents.

At startup, the `PluginManager` discovers all plugins, sorts them topologically,
and builds a scoped DI container for each one. Bindings flow downward from
dependencies to dependents, while set-binding contributions flow upward from
dependents back to the plugin that declared the set.

### Defining a plugin

A plugin implements the `Plugin` interface and is annotated with `@ServiceProvider`
so that it can be discovered at runtime:

```kotlin
@ServiceProvider
object MyPlugin : Plugin {
    override val dependencies: Set<Plugin> = emptySet()

    override fun DI.Builder.api() {
        // Bindings exposed to plugins that depend on this one
        bind<MyService> { singleton { instance<MyService>() } }
    }

    override fun DI.Builder.implementation() {
        // Internal bindings, not visible to other plugins
        bind<MyService> { singleton { MyServiceImpl() } }
    }
}
```

### Assembling plugins

Use `assemblePlugins` to discover and wire all plugins, then retrieve the DI
container for a given plugin:

```kotlin
fun main() {
    val plugins = assemblePlugins {
        loadPlugins()
    }
    val di = plugins.diFor(MyPlugin)

    val myService by di.instance<MyService>()
    myService.doSomething()
}
```

Inside the `assemblePlugins` block you can also filter discovered plugins and
contribute extra bindings to every plugin's DI container:

```kotlin
val plugins = assemblePlugins {
    loadPlugins()
    filterPlugins { it != SomeUnwantedPlugin }
    contributePluginBindings { plugin ->
        bindSingleton<PluginId> { plugin.id }
    }
}
```

### Set-bindings

Plugins can declare a set-binding in `api()` and let their dependents contribute to it:

```kotlin
@ServiceProvider
object CorePlugin : Plugin {
    override fun DI.Builder.api() {
        bindSet<Extension> {}
    }

    override fun DI.Builder.implementation() {}
}

@ServiceProvider
object FeaturePlugin : Plugin {
    override val dependencies = setOf(CorePlugin)

    override fun DI.Builder.api() {
        inBindSet<Extension> {
            add { singleton { MyExtension() } }
        }
    }

    override fun DI.Builder.implementation() {}
}
```

When you resolve `Set<Extension>` from `CorePlugin`'s DI container, it will include
contributions from all of its dependents.

## Using in your projects

### Gradle plugin setup

Syrup relies on [sweet-spi](https://github.com/whyoleg/sweet-spi) and
[KSP](https://github.com/google/ksp) for service discovery.
Add the following plugins to your module's `build.gradle.kts`:

```kotlin
import dev.whyoleg.sweetspi.gradle.withSweetSpi

plugins {
  id("com.google.devtools.ksp") version "2.3.5"
  id("dev.whyoleg.sweetspi") versions "0.1.3"
}

kotlin {
  withSweetSpi()
}
```

The `withSweetSpi()` call configures KSP to generate the service provider metadata
that Syrup uses to discover your plugins at runtime.

### Dependencies

> **Note:** Syrup isn't currently released to Maven Central.
> Please use the `publishToMavenLocal` task for now to add it to your local Maven repository:
> 
> `./gradlew publishToMavenLocal`

Add the appropriate dependency in each module's `build.gradle.kts`:

- Modules that **define plugins** only need the runtime library:

  ```kotlin
  dependencies {
      implementation("io.github.ptitjes.syrup:syrup-runtime:0.1.0")
  }
  ```

- The **application entry point** (where you call `assemblePlugins`) needs the
  host library, which transitively includes the runtime:

  ```kotlin
  dependencies {
      implementation("io.github.ptitjes.syrup:syrup-host:0.1.0")
  }
  ```

## Project structure

This project follows a Gradle multi-module layout:

- **syrup-runtime** -- the runtime library containing the `Plugin` interface and `PluginId`.
  This is the only dependency your plugin modules need.
- **syrup-host** -- the host library that provides `PluginManager` and wires everything together.
  Only the application entry point needs this dependency.
- **sample** -- a sample application demonstrating how to define and use plugins.

The shared build logic lives in `build-logic`.

## Build and run

This project uses [Gradle](https://gradle.org/). You can use the Gradle wrapper
included in the repository:

```bash
# Build and run the sample application
./gradlew run

# Build only
./gradlew build

# Run all checks, including tests
./gradlew check

# Clean all build outputs
./gradlew clean
```

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.
