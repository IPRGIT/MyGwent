plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)


}

android {
    namespace = "com.example.mygwent"
    compileSdk = 35

    viewBinding {
        enable=true
    }

    defaultConfig {
        applicationId = "com.example.mygwent"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {



    implementation (libs.okhttp3.logging.interceptor)
        // UI
        implementation (libs.androidx.core.ktx.v170)
        implementation (libs.androidx.appcompat.v141)
        implementation (libs.material.v150)
        implementation (libs.androidx.constraintlayout.v213)

        // Navigation
        implementation (libs.androidx.navigation.fragment.ktx.v241)
        implementation (libs.androidx.navigation.ui.ktx.v241)

        // Retrofit
        implementation (libs.retrofit)
        implementation (libs.converter.gson)

        // Glide
        implementation (libs.glide.v4130)
        annotationProcessor (libs.compiler.v4130)

        // Coroutines
        implementation (libs.kotlinx.coroutines.android.v160)

        // ViewModel
        implementation (libs.androidx.lifecycle.viewmodel.ktx.v241)
        implementation (libs.androidx.activity.ktx)

        // RecyclerView
        implementation (libs.androidx.recyclerview)




    implementation (libs.androidx.navigation.fragment.ktx.v253)
    implementation (libs.androidx.navigation.ui.ktx.v253)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.glide)
    annotationProcessor(libs.compiler)


    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.appcompat.v170)
    implementation(libs.material.v1120)
    implementation(libs.androidx.constraintlayout.v221)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.espresso.core.v361)
    implementation(kotlin("script-runtime"))


//Cards dependencies
    implementation(libs.androidx.cardview)

//glide - transforma urls en imagenes
    implementation(libs.glide)
    implementation(libs.androidx.preference)
// Glide v4 uses this new annotation processor -- see https://bumptech.github.io/glide/doc/generatedapi.html
    annotationProcessor(libs.compiler)

//NavComponent
    /**val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")*/


    val nav_version = "2.8.5"
// Kotlin
    implementation(libs.androidx.navigation.fragment.ktx)
//implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
//implementation(libs.androidx.navigation.ui.ktx)
// Testing Navigation
    implementation(libs.androidx.navigation.testing)
//implementation(libs.androidx.navigation.testing)


//picasso transforma url en imagen
    implementation(libs.picasso)

//retrofit2 turns your HTTP API into a Java interface
    val retrofit2Version = "2.9.0"
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

//coroutines - Library support for Kotlin coroutines
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.core.ktx.v1150)
    implementation(libs.appcompat.v170)
    implementation(libs.material.v1120)
    implementation(libs.androidx.constraintlayout.v220)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.espresso.core.v361)
}