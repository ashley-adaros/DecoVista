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
        maven { url = java.net.URI("https://jitpack.io") } // Sceneview puede usar Jitpack o mavenCentral
    }
}

rootProject.name = "DecoVista"

// Registro de Módulos
include(":app")

include(":core:database")
include(":core:calculator")
include(":core:model")
include(":core:network")
include(":core:designsystem")
include(":core:ar-engine")

include(":features:catalog")
include(":features:planner2d")
include(":features:viewer3d")
