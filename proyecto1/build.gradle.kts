buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-build-tools-impl:2.0.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}