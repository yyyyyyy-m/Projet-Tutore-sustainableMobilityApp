buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0") // version stable
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

/**
allprojects {

    repositories {
        google()
        mavenCentral()
    }
}
**/

