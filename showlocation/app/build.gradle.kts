plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Required for Firebase integration
}

android {
    namespace = "com.example.showlocation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.showlocation"
        minSdk = 27
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase platform for managing versions of Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))

    // Firebase services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")

    // Google Maps services
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Android UI and libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Ensure that Google Services plugin is applied after the Android plugin
apply(plugin = "com.google.gms.google-services")
