# Syrup

Syrup is a lightweight plugin system for Kotlin Multiplatform.

Plugin objects are discovered automatically at runtime via a ServiceLoader mechanism
(powered by [sweet-spi](https://github.com/whyoleg/sweet-spi)).
They then participate in building a graph of dependency injection (DI) containers
(powered by [Kodein](https://github.com/kosi-libs/Kodein)).

## How it works

Syrup organizes your application as a set of plugins.
Each plugin can define its own exposed types, extension points, extension contributions, and internal bindings.

To ensure modularity and predictable behavior, Syrup follows these encapsulation rules:

1. **Internal bindings**: Any internal bindings are available only to the plugin that defines them.
2. **Exposed types**: Any type exposed by a plugin (via its specification) is available for injection inside its
   **dependent** plugins.
3. **Extension contributions**: When a plugin defines an extension point, contributions from its **dependent** plugins
   are collected and made available back to the plugin that defines the extension point (through its `PluginContext`).

### Defining a plugin

A plugin is an object that implements the `Plugin` interface and is annotated with `@ServiceProvider` so that it can be
discovered at runtime. It defines its contract in `specification()`and its internal bindings in `implementation()`.

```kotlin
object MyExtensionPoint : ExtensionPoint.Plural<MyExtension>(generic())

@ServiceProvider
object MyPlugin : Plugin {
    override val dependencies: Set<Plugin> = emptySet()

    override fun PluginSpecificationBuilder.specification() {
        // Expose a type to dependent plugins
        exposedType<MyService>()

        // Declare an extension point that others can contribute to
        extensionPoint(MyExtensionPoint)
    }

    override fun DI.Builder.implementation() {
        // Internal bindings, not visible to other plugins
        // This provides the implementation for the exposed MyService
        bind<MyService> { singleton { MyServiceImpl(instance()) } }

        // We can also inject the contributions to our extension point here
        bind<SomeInternalStuff> {
            singleton { SomeInternalStuffImpl(instance<Set<MyExtension>>()) }
            // useless type parameters added for clarity here ^^^
        }
    }
}
```

> **Note:** In lots of tests of Syrup, the plugins are defined as local `class`es and `val`s. This is because the Kotlin
> compiler doesn't allow to define local objects. You must use objects in your own code because `sweet-spi` expects you
> to.

### Assembling plugins

Use `assemblePlugins` to discover and wire all plugins, then retrieve the DI
container for a given plugin:

```kotlin
fun main() {
    val plugins = assemblePlugins {
        loadPlugins()
    }

    // Only exposes its own exposed types
    val publicDi = plugins.publicDiFor(MyPlugin)

    // Exposes its private implementation, its exposed types and its dependencies' exposed types,
    // and the contributions to its owned extension points
    val privateDi = plugins.privateDiFor(MyPlugin)

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

### Extension Points

Plugins can define extension points to allow their dependents to contribute functionality.

```kotlin
// Define extension point objects (usually in a shared location near your plugin)
object AnalyticsHandlers : ExtensionPoint.Plural<AnalyticsHandler>(generic())

@ServiceProvider
object AnalyticsPlugin : Plugin {
    override fun PluginSpecificationBuilder.specification() {
        // Declare ownership of the extension point
        extensionPoint(AnalyticsHandlers)
    }

    override fun DI.Builder.implementation() {
        // Inject the contributions into some internal service
        bind<AnalyticsService> {
            singleton { AnalyticsServiceImpl(instance<Set<AnalyticsHandler>>()) }
            // useless type parameters added for clarity here ^^^
            // or simply `singleton { new(::AnalyticsServiceImpl) }`
        }
    }
}

@ServiceProvider
object FirebaseAnalyticsPlugin : Plugin {
    override val dependencies = setOf(AnalyticsPlugin)

    override fun PluginSpecificationBuilder.specification() {
        // Contribute to the extension point defined in CorePlugin
        AnalyticsHandlers {
            contribution<FirebaseAnalyticsHandler>()
        }
    }

    override fun DI.Builder.implementation() {
        bind<FirebaseAnalyticsHandler> { singleton { FirebaseAnalyticsHandler() } }
    }
}
```

Then the plugin that defines the extension point can retrieve the contributions through its `PluginContext`:

```kotlin
// PluginContext is injected inside the plugin's internal DI
class MyService(pluginContext: PluginContext) {
    val analyticsHandlers by pluginContext.contributions(AnalyticsHandlers)

    // use the analyticsHandlers inside MyService
}
```

> **Note:** In lots of tests of Syrup, the extension points are defined as local `val`s. This is because the Kotlin
> compiler doesn't allow to define local objects. You should use objects in your own code (as we do in the sample app)
> because it eases the extension point discovery by other plugin authors (via IDE's subtypes search).

#### Singular vs. plural extension points

Extension points can be singular or plural.

```kotlin
object MySingularExtensionPoint : ExtensionPoint.Singular<MyExtension>()
object MyPluralExtensionPoint : ExtensionPoint.Plural<MyExtension>()
```

#### Optional extension points

Both singular and plural extension points can be defined as optional or not. For example:

```kotlin
extensionPoint(myExtensionPoint, optional = true)
```

- If an **optional** singular extension point `myExtensionPoint` doesn't have any contributions:
    - `PluginContext.contributionOrNull(myExtensionPoint)` returns `null`
    - `PluginContext.contribution(myExtensionPoint)` throws an error
- If a **non-optional** singular extension point `myExtensionPoint` doesn't have any contributions:
    - `PluginContext.contributionOrNull(myExtensionPoint)` throws an error
    - `PluginContext.contribution(myExtensionPoint)` throws an error
- If an **optional** plural extension point `myExtensionPoint` doesn't have any contributions:
    - `PluginContext.contributions(myExtensionPoint)` returns an empty set
- If a **non-optional** plural extension point `myExtensionPoint` doesn't have any contributions:
    - `PluginContext.contributions(myExtensionPoint)` throws an error

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

- **syrup-runtime**: the runtime library containing the `Plugin` interface and `PluginId`.
  This is the only dependency your plugin modules need.
- **syrup-host**: the host library that provides `PluginManager` and wires everything together.
  Only the application entry point needs this dependency.
- **sample**: a sample application demonstrating how to define and use plugins.

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
