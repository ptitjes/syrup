import dev.whyoleg.sweetspi.gradle.withSweetSpi

plugins {
    id("syrup.conventions.kotlin-multiplatform")
    id("syrup.conventions.publishing")
    alias(libs.plugins.ksp)
    alias(libs.plugins.sweetSpi)
}

kotlin {
    withSweetSpi()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.kotlinxEcosystem)

            api(libs.sweetSpiRuntime)
            api(libs.kodein)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
