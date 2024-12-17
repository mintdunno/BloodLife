plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.minh.bloodlife"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.minh.bloodlife"
        minSdk = 24
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // Firebase Authentication
    implementation(libs.firebase.auth)
    // Firestore
    implementation(libs.firebase.firestore)
    // Firebase Analytics
    implementation(libs.firebase.analytics)
    implementation ("com.google.android.gms:play-services-places:17.1.0")
    implementation ("com.google.android.libraries.places:places:3.3.0")
    //noinspection GradleDependency
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.material:material:1.4.0")
}
