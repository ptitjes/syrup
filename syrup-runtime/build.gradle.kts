plugins {
    id("syrup.conventions.kotlin-multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.kotlinxEcosystem)

            api(libs.kodein)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
