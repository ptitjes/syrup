import com.gradleup.librarian.gradle.Librarian
import dev.whyoleg.sweetspi.gradle.withSweetSpi

plugins {
    id("syrup.conventions.kotlin-multiplatform")
    alias(libs.plugins.ksp)
    alias(libs.plugins.sweetSpi)
}

kotlin {
    withSweetSpi()

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

Librarian.module(project)
