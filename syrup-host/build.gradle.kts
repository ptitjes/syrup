import com.gradleup.librarian.gradle.Librarian

plugins {
    id("syrup.conventions.kotlin-multiplatform")
    alias(libs.plugins.mokkery)
}

kotlin {
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

Librarian.module(project)
