plugins {

    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.daniel.appcomprayventa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.daniel.appcomprayventa"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation("com.google.firebase:firebase-bom:32.8.0")
    implementation("com.google.firebase:firebase-auth:20.0.4")
    implementation("com.google.firebase:firebase-database:20.0.4")
    //implementation("com.google.firebase:firebase-storage")
    //implementation("com.google.firebase:firebase-storage:23.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    //implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("com.google.android.libraries.places:places:3.3.0")


    implementation ("com.hbb20:ccp:2.7.0")

    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    //implementation("com.google.firebase:firebase-analytics")
    //implementation(libs.firebase.analytics)




























}