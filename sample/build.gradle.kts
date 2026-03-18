import dev.whyoleg.sweetspi.gradle.withSweetSpi

plugins {
    id("syrup.conventions.kotlin-multiplatform")
    alias(libs.plugins.ksp)
    alias(libs.plugins.sweetSpi)
}

kotlin {
    withSweetSpi()

    jvm {
        @Suppress("OPT_IN_USAGE")
        binaries {
            executable {
                mainClass = "io.github.ptitjes.syrup.sample.app.MainKt"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":syrup-host"))
        }
    }
}
