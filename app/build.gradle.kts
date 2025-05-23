plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
}

android {

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore")
            storePassword = "qwer1234"
            keyAlias = "com.moosik.moo"
            keyPassword = "qwer1234"
        }
        create("release") {
            storeFile = file("../keystore")
            keyAlias = "com.moosik.moo"
            storePassword = "qwer1234"
            keyPassword = "qwer1234"
        }
    }
    namespace = "coom.moosik.mooo"
    compileSdk = 35

    defaultConfig {
        applicationId = "coom.moosik.mooo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        dataBinding = true
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.retrofit.core) // Retrofit 코어 사용
    implementation(libs.retrofit.converter.gson) // Gson 컨버터 사용
    implementation(libs.retrofit.converter.jackson) // Jackson 컨버터 사용

    implementation(libs.material)
    implementation(libs.androidx.ui.tooling.preview.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.compose.constraintlayout)


    implementation(libs.compose)
    implementation(libs.compose.viewbinding)
    implementation(libs.compose.material)
    implementation(libs.compose.preview)
    implementation(libs.compose.material.icons)

    implementation(libs.google.service.map)
    implementation(libs.google.service.location)
    implementation(libs.map.compose)
    implementation(libs.navigation.compose)
    implementation("com.opencsv:opencsv:5.6")
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.serialization.json)

}