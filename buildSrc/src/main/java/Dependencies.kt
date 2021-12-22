object Versions {
    const val androidPlugin = "7.0.4"
    const val kotlin = "1.6.10"
    const val ossLicensesPlugin = "0.10.4"
    const val googleServicesPlugin = "4.3.10"
    const val firebaseCrashlytics = "2.8.1"
    const val firebaseAppDistribution = "2.2.0"

    const val compileSdkVersion = 31
    const val minSdkVersion = 21
    const val targetSdkVersion = 31
}

object Libs {
    // Android X
    const val appCompatV7 = "androidx.appcompat:appcompat:_"
    const val design = "com.google.android.material:material:_"
    const val core = "androidx.core:core:_"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:_"
    const val androidKtx = "androidx.core:core-ktx:_"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:_"
    const val browser = "androidx.browser:browser:_"

    // Play Services
    const val firebaseBom = "com.google.firebase:firebase-bom:_"
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    const val firebaseMessaging = "com.google.firebase:firebase-messaging-ktx"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    const val firebaseRemoteConfig = "com.google.firebase:firebase-config-ktx"

    const val playCore = "com.google.android.play:core:_"

    //Log
    const val timber = "com.jakewharton.timber:timber:_"

    //Open Source mention
    const val ossLicenses = "com.google.android.gms:play-services-oss-licenses:_"

    //image
    const val glide = "com.github.bumptech.glide:glide:4.12.0"
    const val glideCompiler = "com.github.bumptech.glide:compiler:4.12.0"

    // Network
    const val retrofit = "com.squareup.retrofit2:retrofit:2.6.0"
    const val retrofitGson = "com.squareup.retrofit2:converter-gson:2.6.0"
    const val retrofitConverter = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.4.0"
    const val retrofitCoroutines = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
    const val okHttp = "com.squareup.okhttp3:okhttp:3.10.0"
    const val okHttpInterceptor = "com.squareup.okhttp3:logging-interceptor:3.9.0"

    // Kotlin x Coroutines
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib:_"
    const val kxCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:_"
    const val kxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:_"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.0"

}
