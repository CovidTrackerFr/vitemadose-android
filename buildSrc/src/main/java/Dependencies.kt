object Versions {
    const val androidPlugin = "4.1.3"
    const val kotlin = "1.4.32"
    const val googleServicesPlugin = "4.3.4"
    const val firebaseCrashlytics = "2.3.0"
    const val firebaseAppDistribution = "2.0.1"

    const val compileSdkVersion = 30
    const val minSdkVersion = 21
    const val targetSdkVersion = 30
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
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    const val firebaseRemoteConfig = "com.google.firebase:firebase-config-ktx"

    //Log
    const val timber = "com.jakewharton.timber:timber:_"

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
