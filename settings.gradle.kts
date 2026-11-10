pluginManagement {
    repositories {
        google {
            url = uri("https://maven.google.com")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            url = uri("https://maven.google.com")
        }
        mavenCentral()
    }
}

rootProject.name = "CityListen"
include(":app")
include(":domain")
include(":data")