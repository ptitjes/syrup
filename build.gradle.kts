import com.gradleup.librarian.gradle.Librarian

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.librarian) apply false
}

Librarian.root(project)
