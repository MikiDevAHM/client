buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/maven")
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.github.MikiDevAHM:ForgeGradle:3750acd")
        classpath("com.github.thefightagainstmalware:MixinGradle:92e66fe")
    }
}

rootProject.name = "Client"
