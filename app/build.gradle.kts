import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    compileSdkVersion(Versions.compileSdkVersion)

    defaultConfig {
        applicationId = "com.covidtracker.vitemadose"
        setMinSdkVersion(Versions.minSdkVersion)
        setTargetSdkVersion(Versions.targetSdkVersion)
        versionCode = 3
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystores/vitemadose_upload_key.jks")
            storePassword = gradleLocalProperties(rootDir).getProperty("KEYSTORE_VITEMADOSE_STORE_PASSWORD")
            keyAlias = gradleLocalProperties(rootDir).getProperty("KEYSTORE_VITEMADOSE_ALIAS")
            keyPassword = gradleLocalProperties(rootDir).getProperty("KEYSTORE_VITEMADOSE_KEY_PASSWORD")

            firebaseAppDistribution {
                groups = "androidtesters"
            }
        }
        getByName("debug") {
            storeFile = file("../keystores/vitemadose_debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isZipAlignEnabled = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isZipAlignEnabled = true
            isMinifyEnabled = false
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(Libs.kotlinStdLib)

    implementation(Libs.androidKtx)

    implementation(Libs.appCompatV7)
    implementation(Libs.constraintLayout)
    implementation(Libs.design)
    implementation(Libs.core)
    implementation(Libs.swipeRefreshLayout)
    implementation(Libs.browser)

    implementation(Libs.timber)

    implementation(platform(Libs.firebaseBom))
    implementation(Libs.firebaseAnalytics)
    implementation(Libs.firebaseCrashlytics)
    implementation(Libs.firebaseRemoteConfig)

    implementation(Libs.kxCoroutines)
    implementation(Libs.kxCoroutinesAndroid)

    implementation(Libs.retrofit)
    implementation(Libs.retrofitGson)
    implementation(Libs.retrofitConverter)
    implementation(Libs.retrofitCoroutines)
    implementation(Libs.okHttp)
    api(Libs.okHttpInterceptor)

    implementation(Libs.kotlinSerialization)
}