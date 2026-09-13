import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.api.tasks.SourceSet

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.mixin.gradle)
    alias(libs.plugins.shadow)
   // kotlin("kapt") version "2.3.10" //FOR SOME REASON THIS SH*T BREAKS REOBFJAR TASK
}

base {
    version = "Release"
    group = "me.eldodebug"
    archivesName = "GlideClient"
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

kotlin {
    compilerOptions {
        jvmToolchain(8)
        freeCompilerArgs.add("-Xallow-no-source-files")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

val embed by configurations.creating
val compile by configurations.creating
configurations {
    named("implementation") {
        extendsFrom(embed)
    }

    named("annotationProcessor") {
        extendsFrom(embed)
    }

    named("runtimeOnly") {
        isCanBeResolved = true
    }
}

minecraft {
    version = "1.8.9"
    runDir = "run"
    mappings = "stable_22"
    setTweakClass("me.eldodebug.soar.injection.mixin.GlideTweaker")
    setMainClass("net.minecraft.launchwrapper.Launch")
    makeObfSourceJar = false
}

tasks.named<JavaExec>("runClient") {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    if (osName.contains("mac") &&
        (osArch.contains("arm") || osArch.contains("aarch64"))
    ) {
        val bodgePath = file("libs/mac/arm64/natives").absolutePath

        println("[INFO] Apple Silicon Natives path $bodgePath")

        jvmArgs("-Djava.library.path=$bodgePath")
    }

    classpath = files(configurations["embed"], classpath)
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://maven.cleanroommc.com")
}

dependencies {
    embed(kotlin("stdlib"))

    "annotationProcessor"(libs.mixin)

    embed(libs.mixin) {
        exclude(module = "launchwrapper")
        exclude(module = "guava")
        exclude(module = "gson")
        exclude(module = "commons-io")
    }

    embed(files("libs/lwjgl-soar.jar"))
    embed(files("libs/lwjgl-soar-natives.jar"))

    embed("com.github.oshi:oshi-core:6.9.0") {
        exclude(module = "slf4j-api")
    }
    testImplementation(kotlin("test"))
}

mixin {
    defaultObfuscationEnv = "notch"
    add("main", "mixins.soar.refmap.json")
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)

    from(sourceSets.main.get().resources.srcDirs) {
        include("**/*")
    }
}

tasks.named<ShadowJar>("shadowJar") {

    configurations = project.configurations.named("embed").map { listOf(it) }

    relocate("oshi", "com.glideclient.libs.oshi")
    archiveClassifier.set("")

    // THIS WAS NEEDED FOR ME BC THE REFMAP FILE WAS PUT ON A DIFFERENT PLACE
    from(layout.buildDirectory.file("tmp/compileJava/mixins.soar.refmap.json")) {
        into("")
    }

    manifest {
        attributes(
            "MixinConfigs" to "mixins.soar.json",
            "TweakClass" to "me.eldodebug.soar.injection.mixin.GlideTweaker",
            "TweakOrder" to 0,
            "Manifest-Version" to "1.0",
            "FMLAT" to "soar_at.cfg"
        )
    }
}
reobf {
    tasks.named<ShadowJar>("shadowJar") {
    }
}

tasks.named("reobfJar") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named("build") {
    dependsOn("shadowJar")
}