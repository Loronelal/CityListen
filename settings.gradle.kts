pluginManagement {
    repositories {
        // Прямой URL Google Maven (обходим возможные блокировки maven.google.com)
        maven {
            url = uri("https://dl.google.com/dl/android/maven2/")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://dl.google.com/dl/android/maven2/")
        }
        mavenCentral()
    }
}

rootProject.name = "CityListen"
include(":app")
include(":domain")
include(":data")