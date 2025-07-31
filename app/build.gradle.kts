plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.chaquo.python")
}

android {
    namespace = "com.example.ephmonitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ephmonitor"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

chaquopy {
    defaultConfig {
        buildPython ("E:/Python/python/python.exe")
        pip {
            install("numpy")
            install("numba")
            install("resampy==0.3.0")
            install("scipy")
            install("librosa==0.9.2")
            install("torch")
            install("torchvision")
            install("matplotlib")
            install("pandas")
            install("requests")
            install("websocket")
            install("websocket-client")
            install("wheel")
            install("seaborn==0.12.2")
            install("ewtpy")
            install("PyWavelets")
            install("emd==0.4.0")
            install("vmdpy")
            install("opencv-python")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.ui.desktop)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.gridlayout:gridlayout:1.0.0")

    implementation ("androidx.preference:preference-ktx:1.1.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("org.nanohttpd:nanohttpd:2.3.1")
    implementation("javax.activation:activation:1.1.1")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.github.tbruyelle:rxpermissions:0.12")
    implementation("io.reactivex.rxjava3:rxjava:3.1.6")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    implementation("org.pytorch:pytorch_android:1.13.0")
    implementation("org.pytorch:pytorch_android_torchvision:1.13.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
}







