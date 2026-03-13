# syrup

This project uses [Gradle](https://gradle.org/).

To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the sample application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

This project follows the suggested multi-module setup and consists of the subprojects:

- `syrup-runtime` - the runtime library
- `syrup-host` - the host library
- `sample` - a sample application that uses syrup
-

The shared build logic is located in `build-logic`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).
