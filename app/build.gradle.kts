import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dependency.check)
}

val nvdApiKey: String? = System.getenv("NVD_API_KEY")

dependencyCheck {
    failBuildOnCVSS.set(7f)
    skipTestGroups.set(true)
    if (!nvdApiKey.isNullOrBlank()) {
        nvd {
            apiKey.set(nvdApiKey)
        }
    }
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun propertyValue(name: String): String? =
    (providers.gradleProperty(name).orNull ?: localProperties.getProperty(name))
        ?.takeIf { it.isNotBlank() }

val SHA256_BASE64 = Regex("^[A-Za-z0-9+/]{43}=$")

// Certificate pinning for the release variant (R4). Optional: without these
// properties the release build only enforces TLS (cleartext disabled).
val serverPinDomain = propertyValue("SERVER_PIN_DOMAIN")
val serverPinSha256 = propertyValue("SERVER_PIN_SHA256")
val serverPinBackupSha256 = propertyValue("SERVER_PIN_BACKUP_SHA256")
val serverPinExpiration = propertyValue("SERVER_PIN_EXPIRATION")

require((serverPinDomain == null) == (serverPinSha256 == null)) {
    "SERVER_PIN_DOMAIN y SERVER_PIN_SHA256 deben definirse juntos (local.properties o -P)"
}
serverPinSha256?.let {
    require(SHA256_BASE64.matches(it)) {
        "SERVER_PIN_SHA256 debe ser un hash SHA-256 en base64 (44 caracteres)"
    }
}
serverPinBackupSha256?.let {
    require(SHA256_BASE64.matches(it)) {
        "SERVER_PIN_BACKUP_SHA256 debe ser un hash SHA-256 en base64 (44 caracteres)"
    }
}

abstract class GenerateNetworkSecurityConfigTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val domain: Property<String>

    @get:Input
    abstract val pin: Property<String>

    @get:Input
    abstract val backupPin: Property<String>

    @get:Input
    abstract val expiration: Property<String>

    @TaskAction
    fun generate() {
        val xmlFile = outputDir.get().asFile.resolve("xml/network_security_config.xml")
        xmlFile.parentFile.mkdirs()

        val domainValue = domain.get()
        val pinValue = pin.get()
        if (domainValue.isEmpty() || pinValue.isEmpty()) {
            xmlFile.delete()
            return
        }

        val backupPinValue = backupPin.get()
            .takeIf { it.isNotEmpty() }
            ?.let { "\n            <pin digest=\"SHA-256\">$it</pin>" }
            .orEmpty()
        val expirationValue = expiration.get()
            .takeIf { it.isNotEmpty() }
            ?.let { " expiration=\"$it\"" }
            .orEmpty()

        xmlFile.writeText(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<network-security-config>
            |    <base-config cleartextTrafficPermitted="false">
            |        <trust-anchors>
            |            <certificates src="system" />
            |        </trust-anchors>
            |    </base-config>
            |    <domain-config>
            |        <domain includeSubdomains="true">$domainValue</domain>
            |        <pin-set$expirationValue>
            |            <pin digest="SHA-256">$pinValue</pin>$backupPinValue
            |        </pin-set>
            |    </domain-config>
            |</network-security-config>
            |""".trimMargin()
        )
    }
}

val generateReleaseNetworkSecurityConfig =
    tasks.register<GenerateNetworkSecurityConfigTask>("generateReleaseNetworkSecurityConfig") {
        outputDir.set(layout.buildDirectory.dir("generated/res/releaseNetSec"))
        domain.set(serverPinDomain ?: "")
        pin.set(serverPinSha256 ?: "")
        backupPin.set(serverPinBackupSha256 ?: "")
        expiration.set(serverPinExpiration ?: "")
    }

android {
    namespace = "com.fireflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fireflow"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.fireflow.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.sources.res?.addGeneratedSourceDirectory(
            generateReleaseNetworkSecurityConfig
        ) { it.outputDir }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":database"))
    implementation(project(":security"))
    implementation(project(":feature_login"))
    implementation(project(":feature_clientes"))
    implementation(project(":feature_grupos"))
    implementation(project(":feature_revisiones"))
    implementation(project(":feature_curvas"))
    implementation(project(":feature_informes"))
    implementation(project(":feature_exportaciones"))
    implementation(project(":feature_fotografias"))
    implementation(project(":feature_configuracion"))
    implementation(project(":feature_historial"))
    implementation(project(":feature_conversiones"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.datastore)

    ksp(libs.hilt.compiler)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.compose.ui.test.manifest)
}
