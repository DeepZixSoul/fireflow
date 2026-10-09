import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fireflow.database"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(localPropsFile.inputStream())
        }
        // Legacy secret: only used to rekey databases created before the
        // runtime passphrase (DatabasePassphraseProvider). Do not use for new
        // encryption. Remove once no legacy installations remain.
        val passphrase = localProps.getProperty("DB_PASSPHRASE")
            ?: throw GradleException(
                "DB_PASSPHRASE not found in local.properties. " +
                "Add: DB_PASSPHRASE=<your-secret> to local.properties (root of project)."
            )
        buildConfigField("String", "DB_PASSPHRASE", "\"${passphrase}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)
    implementation(libs.security.crypto)
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
    testImplementation(libs.sqlcipher)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
