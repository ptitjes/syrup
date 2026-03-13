# Agent guidelines for Syrup

## Project overview

Syrup is a lightweight plugin system for Kotlin Multiplatform. It combines
[sweet-spi](https://github.com/whyoleg/sweet-spi) for automatic plugin discovery
and [Kodein](https://github.com/kosi-libs/Kodein) for dependency injection.

Plugins implement the `Plugin` interface (in `syrup-runtime`), declare dependencies
on other plugins, and define bindings in two scopes: `api()` (exposed to dependents)
and `implementation()` (private). The `PluginManager` (in `syrup-host`) discovers
plugins via ServiceLoader, topologically sorts them, and builds a per-plugin DI
container graph where bindings flow downward and set-binding contributions flow upward.

## Project structure

- `syrup-runtime` -- runtime library with the `Plugin` interface and `PluginId`.
- `syrup-host` -- host library with `PluginManager` and the internal DI wiring.
- `sample` -- sample application demonstrating plugin definition and usage.
- `build-logic` -- shared Gradle build conventions.

Key source files:

- `syrup-runtime/src/commonMain/kotlin/io/github/ptitjes/syrup/Plugin.kt` -- the `Plugin` interface.
- `syrup-host/src/commonMain/kotlin/io/github/ptitjes/host/PluginManager.kt` -- public `PluginManager` API.
- `syrup-host/src/commonMain/kotlin/io/github/ptitjes/host/internal/DefaultPluginManager.kt` -- plugin loading, topological sort, and DI graph construction.
- `syrup-host/src/commonMain/kotlin/io/github/ptitjes/host/internal/PluginDIs.kt` -- per-plugin DI container setup (public and private scopes).

## Build and run

This project uses [Gradle](https://gradle.org/).

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

This project follows a traditional Gradle multi-module setup. The shared build logic
is located in `build-logic`. Dependencies are declared in `gradle/libs.versions.toml`.
Both the build cache and the configuration cache are enabled (see `gradle.properties`).

## Code conventions

- The project targets Kotlin Multiplatform. Runtime and host modules use `commonMain`
  source sets with JVM and (potentially) other targets.
- Plugins are declared as `object` types annotated with `@ServiceProvider`.
- Internal implementation details in `syrup-host` are marked `internal` and placed
  under the `internal` package.
- Tests use `kotlin-test` and the Mokkery mocking library.
