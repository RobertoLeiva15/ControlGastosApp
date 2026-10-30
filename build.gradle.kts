// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // KSP: genera automáticamente el código de Room a partir de nuestras anotaciones
    id("com.google.devtools.ksp") version "2.3.12" apply false
}