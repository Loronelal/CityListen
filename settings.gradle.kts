pluginManagement {
    repositories {
        // Зеркало Aliyun для Google Maven (обходим блокировки dl.google.com)
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "CityListen"
include(":app")
include(":domain")
include(":data")