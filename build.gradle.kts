plugins {
    id("com.android.application").version(Versions.androidPlugin).apply(false)
    id("org.jetbrains.kotlin.jvm").version(Versions.kotlin).apply(false)
    id("org.jetbrains.kotlin.android").version(Versions.kotlin).apply(false)
    id("org.jetbrains.kotlin.android.extensions").version(Versions.kotlin).apply(false)
    id("com.google.gms.google-services").version(Versions.googleServicesPlugin).apply(false)
    id("com.google.android.gms.oss-licenses-plugin").version(Versions.ossLicensesPlugin).apply(false)
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
