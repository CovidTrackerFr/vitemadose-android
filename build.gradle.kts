plugins {
    id("com.android.application").version(Versions.androidPlugin).apply(false)
    id("org.jetbrains.kotlin.jvm").version(Versions.kotlin).apply(false)
    id("org.jetbrains.kotlin.android").version(Versions.kotlin).apply(false)
    id("org.jetbrains.kotlin.android.extensions").version(Versions.kotlin).apply(false)
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}