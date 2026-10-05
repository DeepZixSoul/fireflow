pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Igrupos"

include(":app")
include(":core")
include(":common")
include(":domain")
include(":data")
include(":database")
include(":security")
include(":feature_login")
include(":feature_clientes")
include(":feature_grupos")
include(":feature_revisiones")
include(":feature_curvas")
include(":feature_informes")
include(":feature_exportaciones")
include(":feature_fotografias")
include(":feature_configuracion")
include(":feature_historial")
include(":feature_conversiones")
