plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.arsalankhan.weatherapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.arsalankhan.weatherapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    /* ---------------- AndroidX Core ---------------- */
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    /* ---------------- Retrofit & Networking ---------------- */
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    /* ---------------- Location Services ---------------- */
    implementation("com.google.android.gms:play-services-location:21.0.1")

    /* ---------------- Image Loading ---------------- */
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    /* ---------------- Room Database ---------------- */
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    /* ---------------- Shimmer Effect ---------------- */
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    /* ---------------- Testing ---------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    /* ---------------- Optional (remove if unused) ---------------- */
    implementation(libs.ink.geometry.jvm)
    implementation(libs.filament.android)
    implementation(libs.sceneform.base)


}
