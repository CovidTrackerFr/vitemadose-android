import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.nio.file.Paths
import org.jetbrains.kotlin.konan.properties.hasProperty

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
        applicationId = "com.cvtracker.vmd2"
        setMinSdkVersion(Versions.minSdkVersion)
        setTargetSdkVersion(Versions.targetSdkVersion)
        versionCode = 6
        versionName = "1.0.1"
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
            if (gradleLocalProperties(rootDir).hasProperty("KEYSTORE_VITEMADOSE_STORE_PASSWORD")) {
                signingConfig = signingConfigs.getByName("release")
            }

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

    flavorDimensions("env", "oss")

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "env"
        }
        create("oss") {
            dimension = "oss"
            applicationIdSuffix = ".oss"
            versionNameSuffix = "-oss"
        }
        create("nonOss") {
            dimension = "oss"
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }

    applicationVariants.all {
        // Strip "NonOss" from output filenames.
        outputs.all {
            this as BaseVariantOutputImpl
            outputFileName = Paths.get(
                "..",
                "..",
                flavorName.replace("NonOss", ""),
                buildType.name,
                outputFileName.replace("nonOss-", "")
            ).toString()
        }

        // Don't require google-services.json and Crashlytics configuration for oss flavors.
        val isOss = (productFlavors.find{ it.dimension == "oss" }?.name == "oss")
        if (isOss) {
            val cname = name.capitalize()
            try {
                project.tasks.getByName("process${cname}GoogleServices").enabled = false
                project.tasks.getByName("uploadCrashlyticsMappingFile${cname}").enabled = false
            } catch (e: UnknownTaskException) {}
        }
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

    "nonOssImplementation"(platform(Libs.firebaseBom))
    "nonOssImplementation"(Libs.firebaseAnalytics)
    "nonOssImplementation"(Libs.firebaseCrashlytics)
    "nonOssImplementation"(Libs.firebaseRemoteConfig)

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