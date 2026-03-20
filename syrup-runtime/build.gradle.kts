import dev.whyoleg.sweetspi.gradle.withSweetSpi
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("syrup.conventions.kotlin-multiplatform")
    id("syrup.conventions.publishing")
    alias(libs.plugins.ksp)
    alias(libs.plugins.sweetSpi)
}

kotlin {
    withSweetSpi()

    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }

    jvm()

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
