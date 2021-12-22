import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = Versions.compileSdkVersion

    defaultConfig {
        applicationId = "com.cvtracker.vmd2"
        minSdk = Versions.minSdkVersion
        targetSdk = Versions.targetSdkVersion
        versionCode = 26
        versionName = "1.4.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystores/vitemadose_upload_key.jks")
            storePassword = gradleLocalProperties(rootDir).getProperty("KEYSTORE_VITEMADOSE_STORE_PASSWORD", null)
            keyAlias = gradleLocalProperties(rootDir).getProperty("KEYSTORE_VITEMADOSE_ALIAS", null)
            keyPassword = gradleLocalProperties(rootDir).getProperty("KEYSTORE_VITEMADOSE_KEY_PASSWORD", null)

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
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }
    }

    flavorDimensions.add("env")

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "env"
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
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
    implementation(Libs.ossLicenses)
    implementation(Libs.glide)
    kapt(Libs.glideCompiler)

    implementation(platform(Libs.firebaseBom))
    implementation(Libs.firebaseAnalytics)
    implementation(Libs.firebaseMessaging)
    implementation(Libs.firebaseCrashlytics)
    implementation(Libs.firebaseRemoteConfig)

    implementation(Libs.playCore)

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
