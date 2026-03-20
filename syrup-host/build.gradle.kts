import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("syrup.conventions.kotlin-multiplatform")
    id("syrup.conventions.publishing")
    alias(libs.plugins.mokkery)
}

kotlin {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.kotlinxEcosystem)

            api(libs.kodein)
            api(project(":syrup-runtime"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
