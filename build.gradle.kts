import nl.javadude.gradle.plugins.license.DownloadLicenses
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish").version("1.2.1")
    id("com.github.hierynomus.license").version("0.16.1")
    id("ru.vyarus.github-info").version("2.0.0")
    id("org.jmailen.kotlinter").version("4.3.0")
}

dependencies {
    implementation("com.hierynomus:sshj:0.38.0")
    implementation("com.jcraft:jzlib:1.1.3")
}

kotlin {
    jvmToolchain(21)
    compilerOptions.jvmTarget = JvmTarget.JVM_1_8
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = "xyz.dussim"
version = "0.0.1"

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

gradlePlugin {
    plugins {
        create("xyz.dussim.gradle-ssh") {
            id = "xyz.dussim.gradle-ssh"
            implementationClass = "xyz.dussim.gradlessh.SshPlugin"

            displayName = "Execute simple ssh commands as Gradle tasks with gradle-ssh"
            tags = listOf("ssh", "remote execution")
            description = "Plugin to execute simple ssh commands as Gradle tasks"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri(".localMaven")
        }
    }
}

github {
    user = "Dussim"
    license = "Apache"
}

license {
    header = file("LICENSE_HEADER")
    strictCheck = true

    ext.set("year", "2024")
    ext.set("name", "Dussim (Artur Tuzim)")
    ext.set("email", "artur@tuzim.xzy")
}

downloadLicenses {
    includeProjectDependencies = false
    dependencyConfiguration = "runtimeClasspath"
}

tasks.withType<DownloadLicenses>()

tasks.withType<KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(JvmTargetValidationMode.ERROR)
}


