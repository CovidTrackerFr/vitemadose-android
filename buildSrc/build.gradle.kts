plugins {
    id("java")
    kotlin("jvm") version "1.4.32"
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
